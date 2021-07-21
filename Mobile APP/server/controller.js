const bcrypt = require("bcryptjs");
var mongoose = require("mongoose");
var multer = require("multer");
var crypto = require("crypto");
const GridFsStorage = require("multer-gridfs-storage");

module.exports = async (app, myDb) => {
  app.post("/signup", async (req, res) => {
    const hash = bcrypt.hashSync(req.body.password, 10);

    const newUser = {
      Name: req.body.name,
      Email: req.body.email,
      Phone: req.body.phone,
      Password: hash,
    };
    const query = { Email: newUser.Email };
    const collection = myDb.collection("signups");
    collection.findOne(query, (err, result) => {
      if (result == null) {
        var randomNumber = Math.random().toString();
        randomNumber = randomNumber.substring(2, randomNumber.length);
        const user = {
          cookieName: newUser.Phone + "_" + randomNumber,
          from: Date.now(),
          to: 900000 + Date.now(),
        };
        myDb.collection("Session").insertOne(user, function (err, resu) {
          if (err) return err;
          else console.log(user);
        });
        res.cookie("cookieName", newUser.Phone + "_" + randomNumber, {
          maxAge: 900000,
          httpOnly: true,
        });
        collection.insertOne(newUser, (err, result) => {
          res.status(200).send();
          res.end(err);
        });
      } else {
        res.status(400).send(result);
      }
    });
  });
  app.get("/", (req, res) => {
    res.setHeader("Content-Type", "text/html");
    res.status(200).send("<h1> STET Application </h1>");
  });

  app.post("/testing", async (req, res) => {
    console.log(req.body);
    res.json({ test: 123 });
  });

  app.post("/login", async (req, res) => {
    try {
      console.log(req.body);
      const query = {
        Phone: req.body.phone,
      };
      const result = await myDb.collection("signups").findOne(query);
      if (result != null) {
        var hash = result.Password;
        const rs = bcrypt.compareSync(req.body.password, hash);
        if (rs) {
          var randomNumber = Math.random().toString();
          randomNumber = randomNumber.substring(2, randomNumber.length);

          const user = {
            cookieName: result.Phone + "_" + randomNumber,
            from: Date.now(),
            to: Date.now() + 900000,
          };
          await myDb.collection("Session").insertOne(user);
          console.log(user);
          res.cookie("cookieName", result.Phone + "_" + randomNumber, {
            maxAge: 7*24*60*60*1000,
            httpOnly: true,
          });
          return res.status(200).send();
        }
      }
      res.status(404).send();
    } catch (err) {
      res.status(500).send();
      console.log(err);
    }
  });
  app.post("/logout", async (req, res) => {
    var cookiez = req.headers.cookiename;
    var cookie = cookiez.substring(11, cookiez.length);
    if (cookie === undefined) {
      return res.status(404).send();
    }
    try {
      const result = await myDb
        .collection("Session")
        .findOne({ cookieName: cookie });
      if (result != null) {
        console.log(result.to);
        res.status(200).send();
      } else {
        res.status(201).send();
      }
    } catch (err) {
      res.status(500).send();
      console.log(err);
    }
  });
  app.post("/session", async (req, res) => {
    var cookiez = req.headers.cookiename;
    var cookie = cookiez.substring(11, cookiez.length);
    if (cookie === undefined) {
      return res.status(404).send();
    }
    try {
      const result = await myDb
        .collection("Session")
        .findOne({ cookieName: cookie });
      if (result != null) {
        // res.sendStatus(200);
        const t = Date.now();
        console.log(t);
        console.log(result.to);
        if (t <= result.to) {
          res.sendStatus(200);
        } else {
          res.sendStatus(201);
        }
      } else {
        res.sendStatus(201);
      }
    } catch (err) {
      res.status(500).send();
      console.log(err);
    }
  });
  app.post("/phone", async (req, res) => {
    try {
      const result = await myDb
        .collection("signups")
        .findOne({ Phone: req.body.phone });
      if (result != null) {
        const objToSend = {
          name: result.Name,
          email: result.Email,
        };
        return res.status(200).send(JSON.stringify(objToSend));
      }
      res.status(404).send();
    } catch (err) {
      res.status(500).send();
      console.log(err);
    }
  });
  app.get("/submitted/:Phone", async (req, res) => {
    try {
      const Phone = req.params.Phone;
      const result = await myDb
        .collection("registration")
        .findOne({
          Phone: Phone, $or: [
            {
              $and: [
                { isRejected: { $exists: true } },
                { isRejected: false }
              ]
            },
            { isRejected: { $exists: false } }
          ]
        });
      if (result == null) {
        console.log(Phone + "not registered");
        return res.sendStatus(200);
      }
      console.log("registered");
      return res.sendStatus(404);
    } catch (err) {
      res.sendStatus(500);
      console.log(err);
    }
  });
  app.post("/check", async (req, res) => {
    try {
      const result = await myDb.collection("signups").findOne({
        $or: [{ Phone: req.body.phone }, { Email: req.body.email }],
      });
      if (result == null) {
        return res.status(400).send();
      }
      const result3 = await myDb
        .collection("signups")
        .findOne({ Phone: req.body.phone });
      if (result3 == null) {
        const result2 = await myDb
          .collection("signups")
          .findOne({ Email: req.body.email });
        if (result2 == null) {
          res.status(400).send();
        } else {
          res.status(202).send();
        }
      } else {
        res.status(201).send();
      }
    } catch (err) {
      res.status(500).send();
      console.log(err);
    }
  });
  app.post("/getpersonal", async (req, res) => {
    try {
      const result = await myDb
        .collection("personals")
        .findOne({ Phone: req.body.Phone });
      if (result == null) {
        return res.status(404).send();
      }
      const objToSend = {
        Fname: result.Fname,
        Mname: result.Mname,
        Lname: result.Lname,
        FHFname: result.FHFname,
        FHMname: result.FHMname,
        FHLname: result.FHLname,
        DOB: result.DOB,
        Community: result.Community,
        Gender: result.Gender,
        Aadhar: result.Aadhar,
        Hno: result.Hno,
        Area: result.Area,
        District: result.District,
        State: result.State,
        Pincode: result.Pincode,
        Phone: result.Phone,
        Email: result.Email,
      };
      res.status(200).send(JSON.stringify(objToSend));
    } catch (err) {
      res.status(500).send();
      console.log(err);
    }
  });

  app.post("/getEducation", async (req, res) => {
    try {
      const result = await myDb
        .collection("academics")
        .findOne({ Phone: req.body.Phone });
      if (result == null) {
        return res.status(404).send();
      }
      const objToSend = {
        ApplicationCategory: result.App_Category,
        PaperLanguage: result.Language,
        TenthPercentage: result.TenthPercentage,
        TwelfthPercentage: result.TwelfthPercentage,
        BScBAPercentage: result.BScBAPercentage,
        BEdPercentage: result.BEdPercentage,
        ProfessionalQualification: result.Pro_Qual,
        University: result.University,
        Phone: result.Phone,
      };
      res.status(200).send(JSON.stringify(objToSend));
    } catch (err) {
      res.status(500).send();
      console.log(err);
    }
  });

  app.get("/getDocuments/:Phone", async (req, res) => {
    try {
      const result = await myDb
        .collection("Twelveth_Documents.files")
        .findOne({ filename: req.params.Phone + "_twelveth.png" });
      if (result == null) {
        return res.status(404).send();
      }
      res.sendStatus(200);
    } catch (err) {
      res.status(500).send();
      console.log(err);
    }
  });

  app.post("/details", async (req, res) => {
    const newUser = {
      Fname: req.body.Fname,
      Mname: req.body.Mname,
      Lname: req.body.Lname,
      FHFname: req.body.FHFname,
      FHMname: req.body.FHMname,
      FHLname: req.body.FHLname,
      DOB: req.body.DOB,
      Community: req.body.Community,
      Gender: req.body.Gender,
      Aadhar: req.body.Aadhar,
      Hno: req.body.Hno,
      Area: req.body.Area,
      District: req.body.District,
      State: req.body.State,
      Pincode: req.body.Pincode,
      Phone: req.body.Phone,
      Email: req.body.Email,
    };
    try {
      const result = await myDb
        .collection("personals")
        .findOne({ Phone: req.body.Phone });
      if (result == null) {
        await myDb.collection("personals").insertOne(newUser);
        return res.status(200).send();
      }
      const User = {
        $set: newUser,
      };
      await myDb
        .collection("personals")
        .updateOne({ Phone: req.body.Phone }, User);
      return res.status(200).send();
    } catch (err) {
      res.status(500).send();
      console.log(err);
    }
  });

  app.post("/education", async (req, res) => {
    const newUser = {
      Min_Qual: req.body.MinQualification,
      Pro_Qual: req.body.ProfessionalQualification,
      TenthPercentage: req.body.TenthPercentage,
      TwelfthPercentage: req.body.TwelfthPercentage,
      BScBAPercentage: req.body.BScBAPercentage,
      BEdPercentage: req.body.BEdPercentage,
      University: req.body.University,
      Language: req.body.PaperLanguage,
      App_Category: req.body.ApplicationCategory,
      Phone: req.body.Phone,
    };
    try {
      const result = await myDb
        .collection("academics")
        .findOne({ Phone: req.body.Phone });
      if (result == null) {
        await myDb.collection("academics").insertOne(newUser);
        return res.status(200).send();
      }
      await myDb
        .collection("academics")
        .updateOne({ Phone: req.body.Phone }, { $set: newUser });
      return res.status(200).send();
    } catch (err) {
      res.status(500).send();
      console.log(err);
    }
  });

  app.post("/timing", async (req, res) => {
    /**
     * @todo eno should be generated on server and in form yearmonthphone
     */
    const newUser = {
      Fname: req.body.fname,
      Mname: req.body.mname,
      Lname: req.body.lname,
      FHFname: req.body.ffname,
      FHMname: req.body.fmname,
      FHLname: req.body.flname,
      Community: req.body.community,
      Aadhar: req.body.aadhar,
      DOB: req.body.dob,
      Hno: req.body.hno,
      Area: req.body.area,
      District: req.body.district,
      State: req.body.state,
      Pincode: req.body.pincode,
      Gender: req.body.gender,
      Phone: req.body.phone,
      Email: req.body.email,
      Exam: req.body.exam,
      Exam_date: req.body.exam_date,
      Venue: req.body.venue,
      Eno: req.body.eno,
      Date: req.body.Date,
    };
    const collection2 = myDb.collection("registration");
    if (newUser != null) {
      collection2.insertOne(newUser, (err, result) => {
        if (err) {
          throw err;
        }
        console.log(newUser);
        res.status(200).send();
      });
    }
  });
  app.get("/available/:filename/:coll", async (req, res) => {
    try {
      /**
       * @todo don't use separate collections for each type of document
       */
      const obj = await myDb
        .collection(req.params.coll + ".files")
        .findOne({ filename: req.params.filename });
      console.log(obj)
      if (obj != null) {
        return res.status(200).send();
      } else {
        return res.status(404).send();
      }
    } catch (err) {
      res.status(500).send();
      console.log(err);
    }
  });
  app.get("/paymentdetails/:phone", async (req, res) => {
    try {
      const result = await myDb
        .collection("Payment_Details")
        .findOne({ Phone: req.params.phone });
      if (result == null) {
        return res.status(404).send();
      }
      res.status(200).json({
        Date: result.Date,
        PaymentId: result.PaymentId,
      });
    } catch (err) {
      res.status(500).send();
      console.log(err);
    }
  });
  /**
   * @todo callback to promise conversion maybe wrong
   */
  app.get("/remove/:filename/:coll", async (req, res) => {
    try {
      const collection = myDb.collection(req.params.coll + ".files");
      const collectionChunks = myDb.collection(req.params.coll + ".chunks");
      const obj = await collection.findOne(
        { filename: req.params.filename },
        { _id: 1 }
      );
      if (obj != null) {
        await collection.deleteOne(obj);
        const obj2 = await collectionChunks.findOne(
          { files_id: obj._id },
          { _id: 1 }
        );
        await collectionChunks.deleteOne(obj2);
        return res.status(200).json(obj2);
      } else {
        return res.status(404).send();
      }
    } catch (err) {
      res.status(500).send();
      console.log(err);
    }
  });

  app.get("/image/:filename/:coll", async (req, res) => {
    let fileName = req.params.filename;
    try {
      const collection = myDb.collection(req.params.coll + ".files");
      const collectionChunks = myDb.collection(req.params.coll + ".chunks");
      const docs = await collection.find({ filename: fileName }).toArray();
      if (!docs || docs.length === 0) {
        return res.status(400).send();
      } else {
        const chunks = await collectionChunks
          .find({ files_id: docs[0]._id })
          .sort({ n: 1 })
          .toArray();
        if (!chunks || chunks.length === 0) {
          return res.status(404).send();
        }
        let fileData = [];
        for (let i = 0; i < chunks.length; i++) {
          fileData.push(chunks[i].data.toString("base64"));
        }
        let finalFile =
          "data:" + docs[0].contentType + ";base64," + fileData.join("");
        console.log("File");
        res.status(200).json({ imageURL: finalFile });
      }
    } catch (err) {
      res.status(500).send();
      console.log(err);
    }
  });

  app.get("/download/:filename/:coll", async (req, res) => {
    let fileName = req.params.filename;
    try {
      const collection = myDb.collection(req.params.coll + ".files");
      const collectionChunks = myDb.collection(req.params.coll + ".chunks");
      const docs = await collection.find({ filename: fileName }).toArray();
      if (!docs || docs.length === 0) {
        return res.status(400).send();
      } else {
        const chunks = await collectionChunks
          .find({ files_id: docs[docs.length - 1]._id })
          .sort({ n: 1 })
          .toArray();
        if (!chunks || chunks.length === 0) {
          return res.status(404).send();
        }
        let fileData = [];
        for (let i = 0; i < chunks.length; i++) {
          fileData.push(chunks[i].data.toString("base64"));
        }
        let finalFile =
          "data:" + docs[0].contentType + ";base64," + fileData.join("");
        console.log("File");
        console.log(docs[0].contentType);
        res.status(200).json({ imageURL: finalFile });
      }
    } catch (err) {
      res.status(500).send();
      console.log(err);
    }
  });

  app.get("/timeline/:year", async (req, res) => {
    try {
      const result = await myDb
        .collection("Timeline")
        .findOne({ year: req.params.year });
      if (result == null) {
        return res.status(404).send();
      }
      const newUser = {
        application_announced_date: result.application_announced_date,
        registration_start_date: result.registration_start_date,
        registration_end_date: result.registration_end_date,
        date_of_admit_card: result.date_of_admit_card,
        date_of_examination: result.date_of_examination,
        date_of_result_declaration: result.date_of_result_declaration,
      };
      res.status(200).send(JSON.stringify(newUser));
    } catch (err) {
      res.status(500).send();
      console.log(err);
    }
  });
  app.post("/resetpassword", async (req, res) => {
    try {
      const result = await myDb
        .collection("signups")
        .findOne({ Phone: req.body.phone });
      if (result == null) {
        return res.status(404).send();
      }
      const hash = bcrypt.hashSync(req.body.password, 10);
      const User = {
        $set: {
          Password: hash,
        },
      };
      await myDb
        .collection("signups")
        .updateOne({ Phone: req.body.phone }, User);
      return res.status(200).send();
    } catch (err) {
      res.status(500).send();
      console.log(err);
    }
  });
  app.post("/payment", async (req, res) => {
    const newUser = {
      Phone: req.body.phone,
      UserContact: req.body.userContact,
      UserEmail: req.body.userEmail,
      Data: req.body.data,
      PaymentId: req.body.paymentId,
      Signature: req.body.signature,
      Amount: req.body.amount,
      Date: req.body.date,
    };
    try {
      const result = await myDb
        .collection("Payment_Details")
        .insertOne(newUser);
      return res.status(200).send();
    } catch (err) {
      res.status(500).send();
      console.log(err);
    }
  });

  const promise = mongoose.connect(process.env.MONGO_URL, {
    useNewUrlParser: true,
    useUnifiedTopology: true,
  });

  const conn1 = mongoose.connection;
  let gfs1;
  conn1.once("open", () => {
    gfs1 = new mongoose.mongo.GridFSBucket(conn1.db, {
      bucketName: "Aadhar_Documents", // Bucketname will be collection name
    });
  });

  storage1 = new GridFsStorage({
    db: promise, //here in case of diskstorage we use storage but in case of GridFS db will be used.
    options: { useUnifiedTopology: true },
    file: (req, file) => {
      return new Promise((resolve, reject) => {
        crypto.randomBytes(16, (err, buf) => {
          if (err) {
            return reject(err);
          }
          const filename = file.originalname;
          const fileInfo = {
            filename: filename,
            bucketName: "Aadhar_Documents",
          };
          resolve(fileInfo);
        });
      });
    },
  });
  app.post(
    "/upload_aadhar",
    multer({
      storage: storage1,
    }).single("upload"),
    function (req, res) {
      console.log(req.file);
      return res.status(200).end();
    }
  );

  const conn2 = mongoose.connection;
  let gfs2;

  conn2.once("open", () => {
    gfs2 = new mongoose.mongo.GridFSBucket(conn2.db, {
      bucketName: "Tenth_Documents", // Bucketname will be collection name
    });
  });

  storage2 = new GridFsStorage({
    db: promise, //here in case of diskstorage we use storage but in case of GridFS db will be used.
    options: { useUnifiedTopology: true },
    file: (req, file) => {
      return new Promise((resolve, reject) => {
        crypto.randomBytes(16, (err, buf) => {
          if (err) {
            return reject(err);
          }
          const filename = file.originalname;
          const fileInfo = {
            filename: filename,
            bucketName: "Tenth_Documents",
          };
          resolve(fileInfo);
        });
      });
    },
  });
  app.post(
    "/upload_tenth",
    multer({
      storage: storage2,
    }).single("upload"),
    function (req, res) {
      console.log(req.file);
      return res.status(200).end();
    }
  );

  const conn3 = mongoose.connection;
  let gfs3;

  conn3.once("open", () => {
    gfs3 = new mongoose.mongo.GridFSBucket(conn3.db, {
      bucketName: "Twelveth_Documents", // Bucketname will be collection name
    });
  });

  storage3 = new GridFsStorage({
    db: promise, //here in case of diskstorage we use storage but in case of GridFS db will be used.
    options: { useUnifiedTopology: true },
    file: (req, file) => {
      return new Promise((resolve, reject) => {
        crypto.randomBytes(16, (err, buf) => {
          if (err) {
            return reject(err);
          }
          const filename = file.originalname;
          const fileInfo = {
            filename: filename,
            bucketName: "Twelveth_Documents",
          };
          resolve(fileInfo);
        });
      });
    },
  });
  app.post(
    "/upload_twelveth",
    multer({
      storage: storage3,
    }).single("upload"),
    function (req, res) {
      console.log(req.file);
      return res.status(200).end();
    }
  );

  const conn4 = mongoose.connection;
  let gfs4;

  conn4.once("open", () => {
    gfs4 = new mongoose.mongo.GridFSBucket(conn4.db, {
      bucketName: "Sikkim_Subject_Documents", // Bucketname will be collection name
    });
  });

  storage4 = new GridFsStorage({
    db: promise, //here in case of diskstorage we use storage but in case of GridFS db will be used.
    options: { useUnifiedTopology: true },
    file: (req, file) => {
      return new Promise((resolve, reject) => {
        crypto.randomBytes(16, (err, buf) => {
          if (err) {
            return reject(err);
          }
          const filename = file.originalname;
          const fileInfo = {
            filename: filename,
            bucketName: "Sikkim_Subject_Documents",
          };
          resolve(fileInfo);
        });
      });
    },
  });
  app.post(
    "/upload_subject",
    multer({
      storage: storage4,
    }).single("upload"),
    function (req, res) {
      console.log(req.file);
      return res.status(200).end();
    }
  );

  const conn5 = mongoose.connection;
  let gfs5;

  conn5.once("open", () => {
    gfs5 = new mongoose.mongo.GridFSBucket(conn5.db, {
      bucketName: "Signature_Documents", // Bucketname will be collection name
    });
  });

  storage5 = new GridFsStorage({
    db: promise, //here in case of diskstorage we use storage but in case of GridFS db will be used.
    options: { useUnifiedTopology: true },
    file: (req, file) => {
      return new Promise((resolve, reject) => {
        crypto.randomBytes(16, (err, buf) => {
          if (err) {
            return reject(err);
          }
          const filename = file.originalname;
          const fileInfo = {
            filename: filename,
            bucketName: "Signature_Documents",
          };
          resolve(fileInfo);
        });
      });
    },
  });
  app.post(
    "/upload_signature",
    multer({
      storage: storage5,
    }).single("upload"),
    function (req, res) {
      console.log(req.file);
      return res.status(200).end();
    }
  );

  const conn6 = mongoose.connection;
  let gfs6;

  conn6.once("open", () => {
    gfs6 = new mongoose.mongo.GridFSBucket(conn6.db, {
      bucketName: "Photo_Documents", // Bucketname will be collection name
    });
  });

  storage6 = new GridFsStorage({
    db: promise, //here in case of diskstorage we use storage but in case of GridFS db will be used.
    options: { useUnifiedTopology: true },
    file: (req, file) => {
      return new Promise((resolve, reject) => {
        crypto.randomBytes(16, (err, buf) => {
          if (err) {
            return reject(err);
          }
          const filename = file.originalname;
          const fileInfo = {
            filename: filename,
            bucketName: "Photo_Documents",
          };
          resolve(fileInfo);
        });
      });
    },
  });
  app.post(
    "/upload_photo",
    multer({
      storage: storage6,
    }).single("upload"),
    function (req, res) {
      console.log(req.file);
      return res.status(200).end();
    }
  );

  const conn7 = mongoose.connection;
  let gfs7;

  conn7.once("open", () => {
    gfs7 = new mongoose.mongo.GridFSBucket(conn7.db, {
      bucketName: "Birth_Certificate_Documents", // Bucketname will be collection name
    });
  });

  storage7 = new GridFsStorage({
    db: promise, //here in case of diskstorage we use storage but in case of GridFS db will be used.
    options: { useUnifiedTopology: true },
    file: (req, file) => {
      return new Promise((resolve, reject) => {
        crypto.randomBytes(16, (err, buf) => {
          if (err) {
            return reject(err);
          }
          const filename = file.originalname;
          const fileInfo = {
            filename: filename,
            bucketName: "Birth_Certificate_Documents",
          };
          resolve(fileInfo);
        });
      });
    },
  });
  app.post(
    "/upload_birth",
    multer({
      storage: storage7,
    }).single("upload"),
    function (req, res) {
      console.log(req.file);
      return res.status(200).end();
    }
  );

  const conn8 = mongoose.connection;
  let gfs8;

  conn8.once("open", () => {
    gfs8 = new mongoose.mongo.GridFSBucket(conn8.db, {
      bucketName: "BSc_BA_Certificate_Documents", // Bucketname will be collection name
    });
  });

  storage8 = new GridFsStorage({
    db: promise, //here in case of diskstorage we use storage but in case of GridFS db will be used.
    options: { useUnifiedTopology: true },
    file: (req, file) => {
      return new Promise((resolve, reject) => {
        crypto.randomBytes(16, (err, buf) => {
          if (err) {
            return reject(err);
          }
          const filename = file.originalname;
          const fileInfo = {
            filename: filename,
            bucketName: "BSc_BA_Certificate_Documents",
          };
          resolve(fileInfo);
        });
      });
    },
  });
  app.post(
    "/upload_bsc_ba",
    multer({
      storage: storage8,
    }).single("upload"),
    function (req, res) {
      console.log(req.file);
      return res.status(200).end();
    }
  );

  const conn9 = mongoose.connection;
  let gfs9;

  conn9.once("open", () => {
    gfs9 = new mongoose.mongo.GridFSBucket(conn9.db, {
      bucketName: "Bed_Certificate_Documents", // Bucketname will be collection name
    });
  });

  storage9 = new GridFsStorage({
    db: promise, //here in case of diskstorage we use storage but in case of GridFS db will be used.
    options: { useUnifiedTopology: true },
    file: (req, file) => {
      return new Promise((resolve, reject) => {
        crypto.randomBytes(16, (err, buf) => {
          if (err) {
            return reject(err);
          }
          const filename = file.originalname;
          const fileInfo = {
            filename: filename,
            bucketName: "Bed_Certificate_Documents",
          };
          resolve(fileInfo);
        });
      });
    },
  });
  app.post(
    "/upload_bed",
    multer({
      storage: storage9,
    }).single("upload"),
    function (req, res) {
      console.log(req.file);
      return res.status(200).end();
    }
  );

  const conn10 = mongoose.connection;
  let gfs10;

  conn10.once("open", () => {
    gfs10 = new mongoose.mongo.GridFSBucket(conn10.db, {
      bucketName: "Community_Certificate_Documents", // Bucketname will be collection name
    });
  });

  storage10 = new GridFsStorage({
    db: promise, //here in case of diskstorage we use storage but in case of GridFS db will be used.
    options: { useUnifiedTopology: true },
    file: (req, file) => {
      return new Promise((resolve, reject) => {
        crypto.randomBytes(16, (err, buf) => {
          if (err) {
            return reject(err);
          }
          const filename = file.originalname;
          const fileInfo = {
            filename: filename,
            bucketName: "Community_Certificate_Documents",
          };
          resolve(fileInfo);
        });
      });
    },
  });
  app.post(
    "/upload_community",
    multer({
      storage: storage10,
    }).single("upload"),
    function (req, res) {
      console.log(req.file);
      return res.status(200).end();
    }
  );
  app.post("/token", async (req, res) => {
    const { Phone, Token } = req.body;
    const response = await myDb
      .collection("firebase-app-token")
      .findOne({ Phone });
    if (response) {
      if (response.Token != Token) {
        await myDb
          .collection("firebase-app-token")
          .updateOne(
            { Phone },
            {
              $set: {
                Phone,
                Token,
                Timestamp: new Date().toLocaleString("en-IN", {
                  timeZone: "Asia/Kolkata",
                }),
              },
            }
          );
        return res.sendStatus(200);
      }
    } else {
      await myDb
        .collection("firebase-app-token")
        .insertOne({
          Phone,
          Token,
          Timestamp: new Date().toLocaleString("en-IN", {
            timeZone: "Asia/Kolkata",
          }),
        });
      return res.sendStaus(200)
    }
  });
};
