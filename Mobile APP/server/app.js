const express = require("express");
const bcrypt = require("bcryptjs");
const app = express();
const bodyParser = require("body-parser");
const cookieParser = require("cookie-parser");
// fs = require('fs-extra')

const mongoClient = require("mongodb").MongoClient;
const app_port = process.env.PORT;
require("events").EventEmitter.prototype._maxListeners = 100;
const controller = require("./controller");
const mongo_url = process.env.MONGO_URL;

const init = async () => {
  try {
    const app = express();
    app.use(bodyParser.urlencoded({ extended: true }));
    app.use(cookieParser());
    app.use(express.json()); //Used to parse JSON bodies
    // app.use(express.urlencoded({ extended: true })); //Parse URL-encoded bodies

    var connection = (
      await mongoClient.connect(mongo_url, {
        useNewUrlParser: true,
        useUnifiedTopology: true,
      })
    ).db("project-stet");
    await controller(app, connection);
    app.listen(app_port, () => {
      console.log(`Listening on port ${app_port}..`);
    });
  } catch (err) {
    console.log(err);
  }
};
init();
