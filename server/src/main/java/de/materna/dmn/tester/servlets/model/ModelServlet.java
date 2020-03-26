package de.materna.dmn.tester.servlets.model;

import com.fasterxml.jackson.core.type.TypeReference;
import de.materna.dmn.tester.drools.helpers.DroolsHelper;
import de.materna.dmn.tester.persistence.WorkspaceManager;
import de.materna.dmn.tester.servlets.filters.ReadAccess;
import de.materna.dmn.tester.servlets.filters.WriteAccess;
import de.materna.dmn.tester.servlets.input.beans.Decision;
import de.materna.dmn.tester.servlets.model.beans.Model;
import de.materna.dmn.tester.servlets.workspace.beans.Workspace;
import de.materna.jdec.dmn.DroolsAnalyzer;
import de.materna.jdec.model.ExecutionResult;
import de.materna.jdec.model.ImportResult;
import de.materna.jdec.model.ModelImportException;
import de.materna.jdec.serialization.SerializationHelper;
import org.apache.log4j.Logger;
import org.kie.dmn.api.core.DMNModel;
import org.kie.dmn.feel.FEEL;
import org.kie.dmn.feel.lang.FEELProfile;
import org.kie.dmn.feel.parser.feel11.profiles.KieExtendedFEELProfile;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;

@Path("/workspaces/{workspace}")
public class ModelServlet {
	private static final Logger log = Logger.getLogger(ModelServlet.class);

	@GET
	@ReadAccess
	@Path("/model")
	@Produces("application/json")
	public Response getModels(@PathParam("workspace") String workspaceUUID) throws IOException {
		Workspace workspace = WorkspaceManager.getInstance().get(workspaceUUID);

		List<Model> models = new LinkedList<>();

		List<DMNModel> dmnModels = DroolsHelper.getModels(workspace);
		for (DMNModel dmnModel : dmnModels) {
			models.add(new Model(dmnModel.getNamespace(), dmnModel.getName(), dmnModel.getDecisions(), dmnModel.getBusinessKnowledgeModels(), dmnModel.getDecisionServices(), workspace.getDecisionSession().getModel(dmnModel.getNamespace(), dmnModel.getName())));
		}

		return Response.status(Response.Status.OK).entity(SerializationHelper.getInstance().toJSON(models)).build();
	}

	@PUT
	@WriteAccess
	@Path("/model")
	@Consumes("application/json")
	public Response importModels(@PathParam("workspace") String workspaceUUID, String body) throws IOException {
		Workspace workspace = WorkspaceManager.getInstance().get(workspaceUUID);

		List<Map<String, String>> models = SerializationHelper.getInstance().toClass(body, new TypeReference<List<Map<String, String>>>() {
		});

		// Save current decision models so we can rollback if the import fails.
		Map<String, String> currentFiles = workspace.getModelManager().getFiles();

		// Clear decision session and the corresponding directory.
		workspace.getModelManager().removeAllFiles();
		workspace.clearDecisionSession();

		try {
			ImportResult importResult = new ImportResult();

			LinkedList<Map<String, String>> importedModels = new LinkedList<>();
			// Import the provided models, collect all import messages.
			for (Map<String, String> model : models) {
				importResult.getMessages().addAll(workspace.getDecisionSession().importModel(model.get("namespace"), model.get("name"), model.get("source")).getMessages());

				String uuid = UUID.randomUUID().toString();
				workspace.getModelManager().persistFile(uuid, model.get("source"));
				model.put("uuid", uuid);
				model.remove("source");

				importedModels.add(model);
			}
			workspace.getConfig().setModels(importedModels);

			// Update the configuration and add an access log entry.
			workspace.getConfig().setModifiedDate(System.currentTimeMillis());
			workspace.getConfig().serialize();
			workspace.getAccessLog().writeMessage("Imported models", workspace.getConfig().getModifiedDate());

			return Response.status(Response.Status.OK).entity(SerializationHelper.getInstance().toJSON(importResult)).build();

		}
		catch (ModelImportException exception) {
			for (Map.Entry<String, String> entry : currentFiles.entrySet()) {
				workspace.getModelManager().persistFile(entry.getKey(), entry.getValue());
			}
			DroolsHelper.initModels(workspace);

			return Response.status(Response.Status.BAD_REQUEST).entity(SerializationHelper.getInstance().toJSON(exception.getResult())).build();
		}
	}

	@GET
	@ReadAccess
	@Path("/model/inputs")
	@Produces("application/json")
	public Response getInputs(@PathParam("workspace") String workspaceUUID) throws IOException {
		Workspace workspace = WorkspaceManager.getInstance().get(workspaceUUID);

		DMNModel model = DroolsHelper.getModel(workspace);
		return Response.status(Response.Status.OK).entity(SerializationHelper.getInstance().toJSON(DroolsAnalyzer.getComplexInputStructure(model))).build();
	}

	@POST
	@ReadAccess
	@Path("/model/execute")
	@Consumes("application/json")
	@Produces("application/json")
	public Response calculateModelResult(@PathParam("workspace") String workspaceUUID, String body) throws IOException {
		Workspace workspace = WorkspaceManager.getInstance().get(workspaceUUID);

		Map<String, Object> inputs = SerializationHelper.getInstance().toClass(body, new TypeReference<HashMap<String, Object>>() {
		});

		ExecutionResult executionResult = workspace.getDecisionSession().executeModel(DroolsHelper.getModel(workspace), inputs);
		return Response.status(Response.Status.OK).entity(SerializationHelper.getInstance().toJSON(executionResult)).build();
	}

	@POST
	@ReadAccess
	@Path("/model/execute/raw")
	@Consumes("application/json")
	@Produces("text/plain")
	public Response calculateRawResult(String body) {
		Decision decision = (Decision) SerializationHelper.getInstance().toClass(body, Decision.class);

		try {
			List<FEELProfile> profiles = new ArrayList<>();
			profiles.add(new KieExtendedFEELProfile());
			FEEL feel = FEEL.newInstance(profiles);

			List<String> messages = new LinkedList<>();
			feel.addListener(feelEvent -> messages.add(feelEvent.getMessage()));

			HashMap<String, Object> outputs = new LinkedHashMap<>();
			outputs.put("main", DroolsHelper.cleanResult(feel.evaluate(decision.getExpression(), decision.getContext())));

			ExecutionResult modelResult = new ExecutionResult(outputs, null, messages);
			return Response.status(Response.Status.OK).entity(SerializationHelper.getInstance().toJSON(modelResult)).build();
		}
		catch (Exception exception) {
			log.error(exception);

			return Response.status(Response.Status.BAD_REQUEST).build();
		}
	}
}