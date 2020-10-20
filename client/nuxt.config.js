const FileSystem = require("fs");
const Path = require("path");
const XML = require('pixl-xml');

const pom = XML.parse(FileSystem.readFileSync(Path.resolve(__dirname, "..", "server", "pom.xml")).toString());

export default async function() {
	return {
		srcDir: './src',
		generate: {
			dir: '../server/src/main/webapp',
		},
		head: {
			meta: [
				{
					charset: 'utf-8'
				},
				{
					name: 'viewport',
					content: "width=device-width,user-scalable=no,initial-scale=1"
				}
			],
			link: [
				{
					rel: 'stylesheet',
					href: 'https://fonts.googleapis.com/css?family=PT+Sans:400,700|Source+Code+Pro:400'
				},
				{
					rel: 'icon',
					href: '/favicon-32x32.png'
				}
			],
			title: "declab"
		},
		mode: "spa",
		router: {
			mode: 'hash'
		},
		plugins: [
			'./plugins/tooltip',
			'./plugins/draggable',
			'./plugins/router',
		],
		env: {
			DECLAB_HOST: (() => {
				return process.env.DECLAB_HOST === undefined ? "http://127.0.0.1:8080/declab-" + process.env.npm_package_version + "/api" : process.env.DECLAB_HOST;
			})(),
			DECLAB_VERSION: process.env.npm_package_version,
			DECLAB_TIME: new Date().toISOString().split("Z")[0].split("T").join(", "),
			DECLAB_JDEC_VERSION: pom.dependencies.dependency.find((dependency) => {
				return dependency.artifactId === "jdec";
			}).version,
		}
	};
}