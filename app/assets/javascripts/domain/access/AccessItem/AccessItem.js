/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

/*
 * Angular factory for a AccessItem.
 */
/* @ngInject */
function AccessItemFactory($q,
                           $log,
                           biobankApi,
                           DomainEntity,
                           ConcurrencySafeEntity,
                           EntityInfo) {
  class AccessItem extends ConcurrencySafeEntity {

    constructor(schema = AccessItem.SCHEMA, obj = {}) {
      super(schema, obj)

      /**
       * A short identifying name that is unique.
       *
       * @name domain.users.AccessItem#name
       * @type {string}
       */

      /**
       * An optional description that can provide additional details on the name.
       *
       * @name domain.users.AccessItem#description
       * @type {string}
       * @default null
       */

      /**
       * This AccessItem's parents.
       *
       * @name domain.users.AccessItem#parentData
       * @type {Array<EntityInfo>}
       */
      this.parentData = []
      if (obj.parentData) {
        this.parentData = obj.parentData.map(info => new EntityInfo(info))
      }

      /**
       * This AccessItem's children.
       *
       * @name domain.users.AccessItem#userData
       * @type {Array<EntityInfo>}
       */
      this.childData = []
      if (obj.childData) {
        this.childData = obj.childData.map(info => new EntityInfo(info))
      }
    }

  }

  AccessItem.SCHEMA = ConcurrencySafeEntity.createDerivedSchema({
    id: 'AccessItem',
    properties: {
      'slug':         { 'type': 'string' },
      'name':         { 'type': 'string' },
      'description':  { 'type': [ 'string', 'null' ] },
      'parentData':   { 'type': 'array', 'items': { '$ref': 'EntityInfo' } },
      'childData':    { 'type': 'array', 'items': { '$ref': 'EntityInfo' } }
    },
    required: [
      'slug',
      'name',
      'parentData',
      'childData'
    ]
  });

  AccessItem.createDerivedSchema = function ({ id, type = 'object', properties = {}, required = {} } = {}) {
    return Object.assign(
      {},
      AccessItem.SCHEMA,
      {
        id: id,
        type: type,
        properties: Object.assign(
          {},
          AccessItem.SCHEMA.properties,
          properties
        ),
        required: AccessItem.SCHEMA.required.slice().concat(required)
      }
    );
  }

  return AccessItem;
}

export default ngModule => ngModule.factory('AccessItem', AccessItemFactory)
