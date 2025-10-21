$version: "2.0"

namespace hellosmithy4s.spec

use alloy#simpleRestJson
use alloy#uuidFormat

@simpleRestJson
service HelloService {
  version: "1.0.0",
  operations: [List, Add, Health]
}

@readonly
@http(method: "GET", uri: "/api/health", code: 200)
operation Health {
  output := {
    @required
    status: String
  }
}


@readonly
@http(method: "GET", uri: "/api/list", code: 200)
operation List {
  output := {
    @required
    @httpPayload
    items: Items
  }
}

list Items {
  member: Item
}

structure Item {
  @required
  name: Name
}

@idempotent
@http(method: "PUT", uri: "/api/create", code: 204)
operation Add {
  input : Item

  errors: [NameAlreadyExists]
}

@error("client")
@httpError(400)
structure NameAlreadyExists {}

string Name
