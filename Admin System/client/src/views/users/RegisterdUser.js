import React, { useState, useEffect } from "react";
import { CCard, CCardBody, CCardHeader, CCol, CRow, CButton, CInput } from "@coreui/react";
import CIcon from "@coreui/icons-react";
import axios from "axios";
import "react-loader-spinner/dist/loader/css/react-spinner-loader.css";
import Loader from "react-loader-spinner";
import { useHistory, useLocation } from "react-router-dom";

const { ServerPORT } = require("../newports");
const uri = "http://localhost:" + ServerPORT;
const RegisteredUser = ({ match }) => {
  const history = useHistory();
  const [user, setUser] = useState({});
  const [isBusy, setBusy] = useState(true);
  const [userDetails, setDetails] = useState({});
  const [approveButtonStatus, disableApproveButton] = useState(false);
  const [rejectButtonStatus, disableRejectButton] = useState(false);
  const [inputFieldStatus, disableinputField] = useState(false);
  var registerdStatus = false;
  //http://localhost:8081
  const url = uri + "/alldetails/" + match.params.id;
  const registerdStatusUrl = uri + "/registered/userdetails/user/" + match.params.id +"/status";
  async function fetchData() {
    if (isBusy) {
      let headers = new Headers();

      headers.append("Content-Type", "application/json");
      headers.append("Accept", "application/json");

      const res = await axios(url, {
        mode: "cors",
        method: "GET",
        headers: headers,
      });
      setUser(res.data);
      const response = await axios(registerdStatusUrl, {
        mode: "cors",
        method: "GET",
        headers: headers,
      })
      registerdStatus = response?.data.Status || false;
      updateButtonsStatus(registerdStatus);
      disableRejectButton(true)
      setBusy(false);
    }
  }
  async function buttonClicked(buttonName) {
    var data = {}
    if (buttonName == "reject") {
      data.isRejected = buttonName == "reject" ? true : false;
      data.Reject_Reason = document.getElementById('input-elem').value;
      data.Status = "Details Rejected"
    } else {
      data.Status = "Details Approved"
    }
    const res = await axios.post(uri + "/registered/user/" + match.params.id + "/statusupdate", data)
    history.push(`/registerd/`)
  }

  function updateButtonsStatus(status) {
    console.log(status)
    if( status ) {
      disableApproveButton(true);
      disableRejectButton(true)
      disableinputField(true);
    }
  }

  useEffect(() => {
    fetchData();
  }, []);

  useEffect(() => {
    setDetails(
      user
        ? Object.entries(user)
        : [
            [
              "id",
              <span>
                <CIcon className="text-muted" name="cui-icon-ban" /> Not found
              </span>,
            ],
          ]
    );
  }, [user]);

  return (
      <>
    <CRow>
      <CCol lg={6}>
        <CCard>
          <CCardHeader>User id: {match.params.id}</CCardHeader>
          {isBusy ? (
            <Loader type="Circles" color="#00BFFF" height={100} width={100} />
          ) : (
            <CCardBody>
              <table className="table table-striped table-hover">
                <tbody>
                  {userDetails.map(([key, value], index) => {
                    return (
                      <tr key={index.toString()}>
                        <td>{`${key}:`}</td>
                        <td>
                          <strong>{value}</strong>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </CCardBody>
          )}
        </CCard>
      </CCol>
    </CRow>
    <CButton component="a" color="success" role="button"  onClick={() => { buttonClicked("approve"); }} disabled={approveButtonStatus}>Approve</CButton>
    <CButton component="a" color="danger" role="button" onClick={() => { buttonClicked("reject"); }} disabled={rejectButtonStatus}>Reject</CButton>
    <CInput
      id="input-elem"
      name="nf-email"
      placeholder="Enter Reject Message"
      disabled={inputFieldStatus}
      onKeyUp={() => { 
        if(!registerdStatus) {
          if( document.getElementById('input-elem').value ) {
            disableApproveButton(true)
            disableRejectButton(false)
          } else {
            disableApproveButton(false)
            disableRejectButton(true)
          }
        }
       }}
    />
    </>
  );
};

export default RegisteredUser;
