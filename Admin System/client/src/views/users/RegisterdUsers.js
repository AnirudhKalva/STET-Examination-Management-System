import React, { useState, useEffect } from "react";

import { useHistory, useLocation } from "react-router-dom";

import {
  CBadge,
  CCard,
  CCardBody,
  CCardHeader,
  CCol,
  CDataTable,
  CRow,
  CPagination,
  CButton,
} from "@coreui/react";
import axios from "axios";
import "react-loader-spinner/dist/loader/css/react-spinner-loader.css";
import Loader from "react-loader-spinner";
const getBadge = (status) => {
  switch (status) {
    case "Details Approved":
    case "Documents Approved":
      return "success";
    case "Details Rejected":
    case "Documents Rejected":
      return "danger";
    default:
      return "warning";
  }
};
const { ServerPORT } = require("../newports");
const uri = "http://localhost:" + ServerPORT;

const RegisterdUsers = () => {
  const history = useHistory();
  const queryPage = useLocation().search.match(/page=([0-9]+)/, "");
  const currentPage = Number(queryPage && queryPage[1] ? queryPage[1] : 1);
  const [page, setPage] = useState(currentPage);
  //const [hasError, setErrors] = useState(false);
  const [UsersData, setData] = useState({});
  const [isBusy, setBusy] = useState(true);
  const [viewButtonStatus, setViewButtonStatus] = useState({});

  const pageChange = (newPage) => {
    currentPage !== newPage && history.push(`/users?page=${newPage}`);
  };

  useEffect(() => {
    currentPage !== page && setPage(currentPage);
  }, [currentPage, page]);

  async function fetchData() {
    if (isBusy === true) {
      let headers = new Headers();

      headers.append("Content-Type", "application/json");
      headers.append("Accept", "application/json");
      //http://localhost:8081
      const res = await axios(uri + "/submittedusers/", {
        mode: "cors",
        method: "GET",
        headers: headers,
      });
      setData(res.data);
      res.data.forEach(({Phone, Status}) => {
        viewButtonStatus[Phone] = Status == "Details Approved" ? false : true;
      });
      setBusy(false);
    }
  }

  useEffect(() => {
    fetchData();
  }, []);

  return (
    <CRow>
      <CCol xl={12}>
        <CCard>
          <CCardHeader>
            Users
            <small className="text-muted"> Registerd on Platform</small>
          </CCardHeader>
          <CCardBody>
            {isBusy ? (
              <Loader type="Circles" color="#00BFFF" height={100} width={100} />
            ) : (
              <CDataTable
                items={UsersData}
                fields={[
                  { key: "Name", _classes: "font-weight-bold" },
                  "Email",
                  "Phone",
                  "Aadhar",
                  "Role",
                  "Status",
                  "Documents",
                ]}
                hover
                striped
                itemsPerPage={7}
                activePage={page}
                clickableRows
                onRowClick={(item) => history.push(`/registerd/user/${item.id}`)}
                scopedSlots={{
                  Status: (item) => (
                    <td>
                      <CBadge
                        color={getBadge(item.Status)}
                        style={{ color: "black", fontSize: "12px" }}
                      >
                        {item.Status}
                      </CBadge>
                    </td>
                  ),
                  Documents: (item) => (
                    <td>
                      <CButton
                        component="a"
                        color="primary"
                        onClick={(e) => {
                          history.push(`/registerd/documents/user/${item.id}`);
                          e.stopPropagation();
                        }}
                        role="button"
                        disabled={viewButtonStatus[item.id]}
                      >
                        View
                      </CButton>
                    </td>
                  ),
                }}
              />
            )}
            <CPagination
              activePage={page}
              onActivePageChange={pageChange}
              pages={5}
              doubleArrows={false}
              align="center"
            />
          </CCardBody>
        </CCard>
      </CCol>
    </CRow>
  );
};

export default RegisterdUsers;
