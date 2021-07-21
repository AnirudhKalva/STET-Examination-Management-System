const mongoose = require("mongoose");
/**
 * @todo remove last fields
 */
const admit_details = mongoose.Schema({
  Fname: {
    type: String,
    required: true,
  },
  Mname: {
    type: String,
  },
  Lname: {
    type: String,
    required: true,
  },
  FHFname: {
    type: String,
    required: true,
  },
  FHMname: {
    type: String,
  },
  FHLname: {
    type: String,
    required: true,
  },
  Aadhar: {
    type: String,
    required: true,
  },
  DOB: {
    type: String,
    required: true,
  },
  Gender: {
    type: String,
    required: true,
  },
  Hno: {
    type: String,
    required: true,
  },
  Area: {
    type: String,
    required: true,
  },
  District: {
    type: String,
    required: true,
  },
  State: {
    type: String,
    required: true,
  },
  Pincode: {
    type: Number,
    required: true,
  },
  Phone: {
    type: String,
    required: true,
  },
  Email: {
    type: String,
    required: true,
  },
  Exam: {
    type: String,
    required: true,
  },
  Exam_date: {
    type: String,
    required: true,
  },
  Venue: {
    type: String,
    required: true,
  },
  Eno: {
    type: String,
    required: true,
  },
  Date: {
    type: Date,
    required: true,
  },
});

module.exports = mongoose.model("admit_card", admit_details, "registration");
