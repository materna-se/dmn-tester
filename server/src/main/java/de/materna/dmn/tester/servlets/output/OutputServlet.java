package de.materna.dmn.tester.servlets.output;

import de.materna.dmn.tester.persistence.PersistenceDirectoryManager;
import de.materna.dmn.tester.persistence.WorkspaceManager;
import de.materna.dmn.tester.servlets.filters.ReadAccess;
import de.materna.dmn.tester.servlets.filters.WriteAccess;
import de.materna.dmn.tester.servlets.output.beans.PersistedOutput;
import de.materna.dmn.tester.servlets.workspace.beans.Workspace;
import de.materna.jdec.serialization.SerializationHelper;
import org.apache.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.UUID;

@Path("/workspaces/{workspace}")
public class OutputServlet {
	private static final Logger log = Logger.getLogger(OutputServlet.class);

	@GET
	@ReadAccess
	@Path("/outputs")
	@Produces("application/json")
	public Response getOutputs(@PathParam("workspace") String workspaceName) {
		try {
			Workspace workspace = WorkspaceManager.getInstance().get(workspaceName);
			
			workspace.getAccessLog().writeMessage("Accessed list of outputs", System.currentTimeMillis());

			return Response.status(Response.Status.OK).entity(workspace.getOutputManager().getFiles()).build();
		}
		catch (IOException exception) {
			log.error(exception);

			return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
		}
	}

	@GET
	@ReadAccess
	@Path("/outputs/{uuid}")
	@Produces("application/json")
	public Response getOutput(@PathParam("workspace") String workspaceName, @PathParam("uuid") String outputUUID) {
		try {
			Workspace workspace = WorkspaceManager.getInstance().get(workspaceName);
			PersistenceDirectoryManager<PersistedOutput> outputManager = workspace.getOutputManager();

			PersistedOutput output = outputManager.getFiles().get(outputUUID);
			if (output == null) {
				throw new NotFoundException();
			}

			workspace.getAccessLog().writeMessage("Accessed output " + outputUUID, System.currentTimeMillis());

			return Response.status(Response.Status.OK).entity(SerializationHelper.getInstance().toJSON(output)).build();
		}
		catch (IOException exception) {
			log.error(exception);

			return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
		}
	}

	@POST
	@WriteAccess
	@Path("/outputs")
	@Consumes("application/json")
	public Response createOutput(@PathParam("workspace") String workspaceName, String body) {
		try {
			Workspace workspace = WorkspaceManager.getInstance().get(workspaceName);
			String uuid = UUID.randomUUID().toString();

			workspace.getOutputManager().persistFile(uuid, (PersistedOutput) SerializationHelper.getInstance().toClass(body, PersistedOutput.class));

			workspace.getAccessLog().writeMessage("Created output " + uuid, System.currentTimeMillis());
			
			return Response.status(Response.Status.CREATED).entity(uuid).build();
		}
		catch (IOException exception) {
			log.error(exception);

			return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
		}
	}

	@PUT
	@WriteAccess
	@Path("/outputs/{uuid}")
	@Consumes("application/json")
	@Produces("application/json")
	public Response editOutput(@PathParam("workspace") String workspaceName, @PathParam("uuid") String outputUUID, String body) {
		try {
			Workspace workspace = WorkspaceManager.getInstance().get(workspaceName);
			PersistenceDirectoryManager<PersistedOutput> outputManager = workspace.getOutputManager();
			if (!outputManager.getFiles().containsKey(outputUUID)) {
				throw new NotFoundException();
			}

			outputManager.persistFile(outputUUID, (PersistedOutput) SerializationHelper.getInstance().toClass(body, PersistedOutput.class));

			workspace.getAccessLog().writeMessage("Edited output " + outputUUID, System.currentTimeMillis());
			
			return Response.status(Response.Status.NO_CONTENT).build();
		}
		catch (IOException exception) {
			log.error(exception);

			return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
		}
	}

	@DELETE
	@WriteAccess
	@Path("/outputs/{uuid}")
	public Response deleteOutput(@PathParam("workspace") String workspaceName, @PathParam("uuid") String outputUUID) {
		try {
			Workspace workspace = WorkspaceManager.getInstance().get(workspaceName);
			PersistenceDirectoryManager<PersistedOutput> outputManager = workspace.getOutputManager();
			if (!outputManager.getFiles().containsKey(outputUUID)) {
				throw new NotFoundException();
			}

			outputManager.removeFile(outputUUID);
			
			workspace.getAccessLog().writeMessage("Deleted output " + outputUUID, System.currentTimeMillis());

			return Response.status(Response.Status.NO_CONTENT).build();
		}
		catch (IOException exception) {
			log.error(exception);

			return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
		}
	}
}