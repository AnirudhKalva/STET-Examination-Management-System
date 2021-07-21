require("dotenv").config();
const express = require("express");
const app = express();
const PORT = 8081; //process.env.PORT||
const ejs = require("ejs");
const bodyParser = require("body-parser");
const fs = require("fs-extra");
var path = require("path");
const mongoClient = require("mongodb").MongoClient;
ObjectId = require("mongodb").ObjectId;
const DB = require("./db.js");
const User = DB.admit_card;
const puppeteer = require("puppeteer");
const Grid = require("gridfs-stream");
const mongoose = require("mongoose");
const mongodb = require("mongodb");
var cors = require("cors");
var nodemailer = require("nodemailer");
var firebaseAdmin = require("firebase-admin");
var serviceAccount = require("./firebaseServiceAccount.json");

app.use(express.static(__dirname + "/public")); // change to whichever directory has images, should contain card.css
app.use(cors());
app.set("view engine", "html");
app.engine("html", ejs.renderFile);
app.set("view engine", "ejs");
app.use(bodyParser.urlencoded({ extended: true }));
app.use(bodyParser.json());

const url = process.env.MONGO_URL;
const back_url = "http://localhost:" + PORT;
var pdfData = {}
const documentCollections = {
  "Photo": { collection_name: "Photo_Documents", file_name_end: "_photo.png" },
  "Birth Certificate": {
    collection_name: "Birth_Certificate_Documents",
    file_name_end: "_birthcertificate.png",
  },
  "Community Certificate": {
    collection_name: "Community_Certificate_Documents",
    file_name_end: "_communitycertificate.png",
  },
  "Signature": {
    collection_name: "Signature_Documents",
    file_name_end: "_signature.png",
  },
  "Aadhar Card": {
    collection_name: "Aadhar_Documents",
    file_name_end: "_aadhar.png",
  },
  "Tenth Memo": {
    collection_name: "Tenth_Documents",
    file_name_end: "_tenth.png",
  },
  "Twelfth Memo": {
    collection_name: "Twelveth_Documents",
    file_name_end: "_twelveth.png",
  },
  "B.Ed Certificate": {
    collection_name: "Bed_Certificate_Documents",
    file_name_end: "_bedcertificate.png",
  },
  "B.Sc BA Certificate": {
    collection_name: "BSc_BA_Certificate_Documents",
    file_name_end: "_bscbacertificate.png",
  },
};
firebaseAdmin.initializeApp({
  credential: firebaseAdmin.credential.cert(serviceAccount),
});
const conn = mongoose.createConnection(url);

// Init gfs
let buffer;

var transporter = nodemailer.createTransport({
  service: "gmail",
  auth: {
    user: process.env.MAIL_USERNAME,
    pass: process.env.MAIL_PASSWORD,
  },
});

if (process.env.NODE_ENV === "production") {
  app.use(express.static("client/build/"));
  app.get("*", (req, res) => {
    res.sendFile(path.resolve(__dirname, "client", "build", "index.html"));
  });
}

mongoClient.connect(
  url,
  { useNewUrlParser: true, useUnifiedTopology: true },
  (err, db) => {
    if (err) {
      console.log("error in connecting to mongodb");
    } else {
      console.log("Connected");
      const myDb = db.db("project-stet");

      app.get("/total", (req, res) => {
        const collection = myDb.collection("signups");
        collection.count({}, function (err, result) {
          if (err) {
            res.send(err);
          } else {
            res.json({
              total: result,
            });
          }
        });
      });

      app.get("/stats", (req, res) => {
        Details = {
          Gender: {},
          Exam: {},
          Community: {},
        };
        const collection = myDb.collection("registration");

        var options = {
          projection: {
            Gender: 1,
            Community: 1,
            Exam: 1,
          },
        };

        collection.find({}, options).toArray(function (err, result) {
          if (err) {
            console.log(err);
            res.status(400).json("Error: " + err);
          } else {
            Details.Gender.Male = result.filter(
              (obj) => obj.Gender === "Male"
            ).length;
            Details.Gender.Female = result.filter(
              (obj) => obj.Gender === "Female"
            ).length;
            Details.Gender.Others = result.filter(
              (obj) => obj.Gender === "Others"
            ).length;
            Details.Exam.primary = result.filter(
              (obj) => obj.Exam === "Primary Teacher"
            ).length;
            Details.Exam.tgt = result.filter(
              (obj) => obj.Exam === "Trained Graduate Teacher"
            ).length;
            Details.Community.general = result.filter(
              (obj) => obj.Community === "General"
            ).length;
            Details.Community.obc = result.filter(
              (obj) => obj.Community === "OBC"
            ).length;
            Details.Community.sc = result.filter(
              (obj) => obj.Community === "SC"
            ).length;
            Details.Community.st = result.filter(
              (obj) => obj.Community === "ST"
            ).length;
            Details.Amount = result.length * 400;
            Details.Registered = result.length;
          }
          res.send(Details);
        });
      });

      app.get("/alldetails/:user", async (req, res) => {
        var User = {};
        try {
          var result = await myDb
            .collection("personals")
            .findOne({ Phone: req.params.user });
          if (result != null) {
            User["First Name"] = result.Fname;
            User["Middle Name"] = result.Mname;
            User["Last Name"] = result.Lname;
            User.Gender = result.Gender;
            User["Father First Name"] = result.FHFname;
            User["Father Middle Name"] = result.FHMname;
            User["Father Last Name"] = result.FHLname;
            User["DOB"] = result.DOB;
            User["Community"] = result.Community;
            User["Aadhar"] = result.Aadhar;
            User.Hno = result.Hno;
            User.Area = result.Area;
            User.District = result.District;
            User.State = result.State;
            User.Pincode = result.Pincode;
            User.Phone = result.Phone;
            User.Email = result.Email;
          }
          var result = await myDb
            .collection("academics")
            .findOne({ Phone: req.params.user });
          if (result != null) {
            User["Application Category"] = result.App_Category;
            User["Exam Language"] = result.Language;
            User["Tenth Percentage"] = result.TenthPercentage;
            User["Twelfth Percentage"] = result.TwelfthPercentage;
            User["B.Sc BA Percentage"] = result.BScBAPercentage;
            User["B.Ed Percentage"] = result.BEdPercentage;
            User["Professional Qualification"] = result.Pro_Qual;
            User["University"] = result.University;
            // User["Minimim Qualification"] = result.Min_Qual;
          }

          var result = await myDb
            .collection("Payment_Details")
            .findOne({ Phone: req.params.user, isRejected: { $exists: false } });
          if (result != null) {
            User.UserContact = result.UserContact;
            User.PaymentEmail = result.UserEmail;
            User.PaymentId = result.PaymentId;
            User.Payment_date = result.Sate;
            // User.Payment_signature = result.Signature
            User.Amount = result.amount;
          }

          var result = await myDb
            .collection("registration")
            .findOne({ Phone: req.params.user, isRejected: { $exists: false } });
          if (result != null) {
            User["Final Submission Date"] = result.Date;
          }
          res.send(JSON.stringify(User));
        } catch (e) {
          console.log(e);
        }
      });

      app.get("/allusers", (req, res) => {
        var array = [];
        myDb
          .collection("signups")
          .find({})
          .toArray(function (err, result) {
            if (err) {
              return;
            } else {
              var count = result.length;
              result.forEach((item) => {
                var newUser = {
                  id: item.Phone,
                  Name: item.Name,
                  Email: item.Email,
                  Phone: item.Phone,
                };
                array.push(newUser);
              });
              res.send(JSON.stringify(array));
            }
          });
      });

      app.get("/submittedusers", (req, res) => {
        var array = [];
        myDb
          .collection("registration")
          .find({})
          .toArray(function (err, result) {
            if (err) {
              return;
            } else {
              var i = 0;
              var count = result.length;
              result.forEach((item) => {
                let newUser = {
                  id: item.Phone,
                  Name: item.Fname + " " + item.Lname,
                  Email: item.Email,
                  Aadhar: item.Aadhar,
                  Phone: item.Phone,
                  Role: item.Exam,
                  Status: item.Status || "Pending",
                };
                array.push(newUser);
              });
              res.send(JSON.stringify(array));
            }
          });
      });

      app.get("/documentnames", async (req, res) => {
        const documentNames = Object.keys(documentCollections);
        res.json(documentNames);
      });

      app.get("/document/:document_name/user/:user_id", async (req, res) => {
        const gfs = Grid(conn.db, mongoose.mongo);
        const user_id = req.params.user_id;
        const document_name = req.params.document_name;
        const collectionProps = documentCollections[document_name];
        gfs.collection(collectionProps.collection_name);
        gfs.files.findOne(
          { filename: user_id + collectionProps.file_name_end, isRejected: { $exists: false } },
          (err, file) => {
            // Check if file
            if (!file || file.length === 0) {
              return res.status(404).json({
                err: "No file exists",
              });
            }
            const readstream = gfs.createReadStream(file.filename);
            readstream.pipe(res);
          }
        );
      });

      //=============== PDF ROUTES BEGIN =========================================

      app.get(
        "/pdf",
        function (req, res) {
          res.render("ejs_admit.html", { data: pdfData });
        }
      );

      async function findData({ Phone }) {
        try {
          const user = await myDb.collection("registration").findOne({ Phone: Phone, isRejected: false, Status: "Documents Approved" });
          if (user == null) return null;
          else {
            user.Address = user.Hno + "," +
              user.Area + "," +
              user.District + "," +
              user.State + "," +
              user.Pincode
            user.img = `${user.Phone}_photo.png`
            return user;
          }
        } catch (err) {
          res.status(500).send();
          console.log(err);
        }
      }
      app.post("/pdf/generate", async function (req, res) {
        try {
          var Phone = req.body.Phone;
          const data = await findData({ Phone }); // declare function
          if (data == null) {
            console.log("Phone number not found");
            res.sendStatus(404);
          } else {
            const browser = await puppeteer.launch(); // run browser
            const page = await browser.newPage(); // create new tab
            pdfData = { ...data, simg: "/stet_logo.jpg" }
            await page.goto(
              back_url + `/pdf`,
              { waitUntil: "load", timeout: 0 }
            ); // go to page
            await page.emulateMedia("screen"); // use screen media
            buffer = await page.pdf({
              path: data.Phone + "_admit.pdf",
              displayHeaderFooter: true,
              printBackground: true,
            }); // generate pdf
            await browser.close(); // close browser

            var bucket = new mongodb.GridFSBucket(myDb);
            fs.createReadStream("./" + data.Phone + "_admit.pdf")
              .pipe(
                bucket.openUploadStream(data.Phone + "_admit.pdf", {
                  contentType: "application/pdf",
                })
              )
              .on("finish", function () {
                console.log("done!");
              });
            fs.unlink("./" + data.Phone + "_admit.pdf", (err) => {
              if (err) {
                console.error(err);
                return;
              }
            });
            const title = "Admit card generated successfully";
            const text =
              "Your admit card was generated successfully. Please download by logging in to the app.";
            await sendMailNotification(data.Email, title, text)

            await sendMobileAppNotification(data.Phone, title, text, "DownloadAdmitcardActivity")
            res.sendStatus(200);
          }
        } catch (e) {
          console.log(e);
         }
      });
      app.post("/pdf/regenerate", async function (req, res) {
        console.log(req.body);
        var phone = req.body.phone;
        const data = await findData({ phone });
        if (data == null) {
          console.log("Phone number not found");
          res.sendStatus(404);
        } else {
          console.log(data);
          await deletePDF(data.phone);
          (async () => {
            const browser = await puppeteer.launch(); // run browser
            const page = await browser.newPage(); // create new tab
            pdfData = { ...data, simg: "/stet_logo.jpg" }
            await page.goto(
              back_url + `/pdf`,
              { waitUntil: "load", timeout: 0 }
            ); // go to page
            await page.emulateMedia("screen"); // use screen media
            buffer = await page.pdf({
              path: data.phone + "_admit.pdf",
              displayHeaderFooter: true,
              printBackground: true,
            }); // generate pdf
            await browser.close(); // close browser
            var bucket = new mongodb.GridFSBucket(myDb);

            fs.createReadStream("./" + data.phone + "_admit.pdf")
              .pipe(bucket.openUploadStream(data.phone + "_admit.pdf"))
              .on("finish", function () {
                console.log("done!");
              });
            fs.unlink("./" + data.phone + "_admit.pdf", (err) => {
              if (err) {
                console.error(err);
                return;
              }
            });
            const title = "Admit card generated successfully";
            const text =
              "Your admit card was generated successfully. Please download by logging in to the app.";
            await sendMailNotification(data.Email, title, text)

            await sendMobileAppNotification(data.Phone, title, text, "DownloadAdmitcardActivity")
            res.sendStatus(200);
          })();
        }
      });
      app.post("/generate_all", async function (req, res) {
        User.find({ isRejected: false, Status: "Documents Approved" }, async (err, users) => {
          if (err) console.log(err);

          users.map(async function (user) {
            const data = {
              fname: user.fname,
              lname: user.lname,
              ffname: user.ffname,
              flname: user.flname,
              dob: user.dob,
              img: `${user.phone}_photo.png`,
              exam: user.exam,
              eno: user.eno,
              address: user.address,
              venue: user.venue,
              exam_date: user.exam_date,
              sex: user.sex,
            };
            const browser = await puppeteer.launch(); // run browser
            const page = await browser.newPage(); // create new tab
            pdfData = { ...data, simg: "/stet_logo.jpg" }
            await page.goto(
              back_url + `/pdf`,
              { waitUntil: "load", timeout: 0 }
            ); // go to page
            await page.emulateMedia("screen"); // use screen media
            buffer = await page.pdf({
              path: user.phone + "_admit.pdf",
              displayHeaderFooter: true,
              printBackground: true,
            }); // generate pdf
            //upload.single(buffer);
            await browser.close(); // close browser
            var bucket = new mongodb.GridFSBucket(myDb);

            fs.createReadStream("./" + user.phone + "_admit.pdf")
              .pipe(bucket.openUploadStream(user.phone + "_admit.pdf"))
              .on("finish", function () {
                console.log("done!");
              });
            fs.unlink("./" + user.phone + "_admit.pdf", (err) => {
              if (err) {
                console.error(err);
                return;
              }
            });
            const title = "Admit card generated successfully";
            const text =
              "Your admit card was generated successfully. Please download by logging in to the app.";
            await sendMailNotification(user.Email, title, text)

            await sendMobileAppNotification(user.Phone, title, text, "DownloadAdmitcardActivity")
          });
          res.send(200);
        })
      });

      app.post("/regenerate_all", function (req, res) {
        var myquery = {};
        myDb.collection("fs.files").deleteMany(myquery, function (err, obj) {
          if (err) throw err;
          console.log(obj.result.n + " document(s) deleted");
        });
        myDb.collection("fs.chunks").deleteMany(myquery, function (err, obj) {
          if (err) throw err;
          console.log(obj.result.n + " document(s) deleted");
        });

        User.find({}, async (err, users) => {
          if (err) console.log(err);

          users.map(async function (user) {
            const data = {
              fname: user.fname,
              lname: user.lname,
              ffname: user.ffname,
              flname: user.flname,
              dob: user.dob,
              img: `${user.phone}_photo.png`,
              exam: user.exam,
              eno: user.eno,
              address: user.address,
              venue: user.venue,
              exam_date: user.exam_date,
              sex: user.sex,
            };
            const browser = await puppeteer.launch(); // run browser
            const page = await browser.newPage(); // create new tab
            pdfData = { ...data, simg: "/stet_logo.jpg" }
            await page.goto(
              back_url + `/pdf`,
              { waitUntil: "load", timeout: 0 }
            ); // go to page
            await page.emulateMedia("screen"); // use screen media
            buffer = await page.pdf({
              path: user.phone + "_admit.pdf",
              displayHeaderFooter: true,
              printBackground: true,
            }); // generate pdf
            //upload.single(buffer);
            await browser.close(); // close browser
            var bucket = new mongodb.GridFSBucket(myDb);

            fs.createReadStream("./" + user.phone + "_admit.pdf")
              .pipe(bucket.openUploadStream(user.phone + "_admit.pdf"))
              .on("finish", function () {
                console.log("done!");
              });
            fs.unlink("./" + user.phone + "_admit.pdf", (err) => {
              if (err) {
                console.error(err);
                return;
              }
            });
            const title = "Admit card generated successfully";
            const text =
              "Your admit card was generated successfully. Please download by logging in to the app.";
            await sendMailNotification(data.Email, title, text)

            await sendMobileAppNotification(data.Phone, title, text, "DownloadAdmitcardActivity")
          });
          res.send(200);
        })
      });

      app.get("/pdf/admit_card.pdf", function (req, res) {
        res.send(buffer);
      });

      // app.post("/register", async function (req, res) {
      //   // if (await User.findOne({ username: req.body.eno })) {
      //   //     throw 'Username "' + req.body.eno + '" is already taken';
      //   // }
      //   const user = new User(req.body);
      //   await user
      //     .save()
      //     .then(() => res.json({}))
      //     .catch((err) => next(err));
      // });

      app.get("/registered/:page/user/:id/status/", async (req, res) => {
        try {
          const user_id = req.params.id;
          const page = req.params.page;
          const data = { "Phone": user_id }
          if (page == "documents") {
            data.Status = "Details Approved"
          } else if (page == "userdetails") {
            data.Status = { $exists: false }
          }
          const result = await myDb.collection("registration").findOne(data, {
            projection: {
              _id: false,
              Status: true
            }
          })
          const Status = result ? false : true
          return res.json({ Status });
        } catch (err) {
          res.status(500).send();
          console.log(err);
        }
      })

      app.post("/registered/user/:id/statusupdate", async (req, res) => {
        const user_id = req.params.id;
        const isRejected = req.body.isRejected || false;
        const rejectReason = req.body.Reject_Reason;
        const status = req.body.Status
        const data = {
          "Status": status,
          "isRejected": isRejected
        }
        if (isRejected) {
          data.Reject_Reason = rejectReason;
        }
        try {
          const result = await myDb
            .collection("registration").updateOne({ "Phone": user_id }, { $set: data })
          if (result.result.nModified == 1) {
            const subject = "Exam Application Status Update"
            const text = isRejected ? rejectReason : status
            await sendMobileAppNotification(user_id, subject, text)
            const userData = await myDb.collection("signups").findOne({ Phone: user_id }, {
              projection: {
                _id: false,
                Email: true
              }
            })
            await sendMailNotification(userData.Email, subject, text)
            return res.json({ "success": true, "message": "Status update successful" })
          } else {
            return res.json({ "success": false, "message": "Status update unsuccessful" })
          }
          return res.status(200).send();
        } catch (err) {
          res.status(500).send();
          console.log(err);
        }
      })

      app.get("/image/:filename", function (req, res) {
        const gfs = Grid(conn.db, mongoose.mongo);
        gfs.collection("Photo_Documents");
        gfs.files.findOne({ filename: req.params.filename }, (err, file) => {
          // Check if file
          if (!file || file.length === 0) {
            return res.status(404).json({
              err: "No file exists",
            });
          }
          const readstream = gfs.createReadStream(file.filename);
          readstream.pipe(res);
        });
      });

      async function deletePDF(phone) {
        return new Promise(async function (resolve, reject) {
          var fileid;
          var myquery = { filename: phone + "_admit.pdf" };
          var foundfile = await myDb.collection("fs.files").findOne(myquery);
          console.log(foundfile);
          fileid = foundfile._id;
          myDb.collection("fs.files").deleteOne(myquery, function (err, obj) {
            if (err) throw err;
            console.log(obj.result.n + " document(s) deleted");
          });
          myquery = { files_id: fileid };
          myDb.collection("fs.chunks").deleteMany(myquery, function (err, obj) {
            if (err) throw err;
            console.log(obj.result.n + " document(s) deleted");
          });
          resolve(1);
        });
      }
      //=================== PDF ROUTES END ==================================

      async function sendMailNotification(toEmail, subject, text) {
        var mailOptions = {
          from: process.env.MAIL_USERNAME,
          to: toEmail,
          subject: subject,
          text: text,
        };
        transporter.sendMail(mailOptions, function (error, info) {
          if (error) {
            console.log(error);
          } else {
            console.log("Email sent: " + info.response);
          }
        });
      }

      // https://stackoverflow.com/a/39818585/9160306
      async function sendMobileAppNotification(Phone, title, text, click_action = "MainActivity") {
        var payload = {
          notification: {
            title,
            body: text,
            click_action,
          },
        };
        const response = await myDb
          .collection("firebase-app-token")
          .findOne({ Phone });
        if (response) {
          const response2 = await firebaseAdmin
            .messaging()
            .sendToDevice(response.Token, payload);
          console.log(response2.successCount);
        } else {
          console.log(`Firebase token not found for user : ${Phone} `);
        }
      }
    }
  }
);

app.listen(PORT, function () {
  console.log("Server is running on Port: " + PORT);
});
