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
} from "@coreui/react";
import axios from "axios";

import "react-loader-spinner/dist/loader/css/react-spinner-loader.css";
import Loader from "react-loader-spinner";
const getBadge = (status) => {
  switch (status) {
    case "Submitted":
      return "success";
    case "Pending":
      return "warning";
    default:
      return "primary";
  }
};
const { ServerPORT } = require("../newports");
const uri = "http://localhost:" + ServerPORT;
const Users = () => {
  const history = useHistory();
  const queryPage = useLocation().search.match(/page=([0-9]+)/, "");
  const currentPage = Number(queryPage && queryPage[1] ? queryPage[1] : 1);
  const [page, setPage] = useState(currentPage);
  const [UsersData, setUsersData] = useState({});
  const [isBusy, setBusy] = useState(true);
  const pageChange = (newPage) => {
    currentPage !== newPage && history.push(`/users?page=${newPage}`);
  };

  useEffect(() => {
    currentPage !== page && setPage(currentPage);
  }, [currentPage, page]);

  async function fetchData() {
    if (isBusy) {
      let headers = new Headers();

      headers.append("Content-Type", "application/json");
      headers.append("Accept", "application/json");
      //http://localhost:8081
      const res = await axios(uri + "/allusers/", {
        mode: "cors",
        method: "GET",
        headers: headers,
      });
      setUsersData(res.data);
      setBusy(false);
    }
  }

  useEffect(() => {
    fetchData();
    console.log(UsersData);
  }, []);

  return (
    <CRow>
      <CCol xl={12}>
        <CCard>
          <CCardHeader>
            Users
            <small className="text-muted"> Signed up on Platform</small>
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
                  "Submitted",
                ]}
                hover
                striped
                itemsPerPage={10}
                activePage={page}
                clickableRows
                onRowClick={(item) => history.push(`/users/${item.id}`)}
                scopedSlots={{
                  Submitted: (item) => (
                    <td>
                      <CBadge color={getBadge(item.Submitted)}>
                        {item.Submitted}
                      </CBadge>
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

export default Users;
