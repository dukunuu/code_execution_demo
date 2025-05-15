import { type RouteConfig, index, route } from "@react-router/dev/routes";

export default [
  index("routes/home.tsx"),
  route(":snippetId", "routes/view.tsx"),
  route("create", "routes/create.tsx"),
] satisfies RouteConfig;
