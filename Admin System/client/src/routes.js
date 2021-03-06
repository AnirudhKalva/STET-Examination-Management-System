import React from "react";

const Dashboard = React.lazy(() => import("./views/dashboard/Dashboard"));
const Users = React.lazy(() => import("./views/users/Users"));
const RegisterdUsers = React.lazy(() => import("./views/users/RegisterdUsers"));
const RegisterdUser = React.lazy(() => import("./views/users/RegisterdUser"));
const Documents = React.lazy(() => import("./views/users/Documents"));
const User = React.lazy(() => import("./views/users/User"));
const Controls = React.lazy(() => import("./views/controls/controls"));
const routes = [
  { path: "/", exact: true, name: "Home" },
  { path: "/dashboard", name: "Dashboard", component: Dashboard },
  { path: "/users", exact: true, name: "Users", component: Users },
  {
    path: "/registerd",
    exact: true,
    name: "Registerd Users",
    component: RegisterdUsers,
  },
  {
    path: "/registerd/documents/user/:id",
    exact: true,
    name: "Documents",
    component: Documents,
  },
  {
    path: "/registerd/user/:id",
    exact: true,
    name: "Documents",
    component: RegisterdUser,
  },
  { path: "/users/:id", exact: true, name: "User Details", component: User },
  {
    path: "/controls",
    exact: true,
    name: "Admin Controls",
    component: Controls,
  },
];

export default routes;
