/**
 * Jasmine test suite
 *
 */
/* global angular */

import * as sharedBehaviour from '../../../test/behaviours/entityNameSharedBehaviour'
import ngModule from '../../index'

describe('CollectionEventTypeName', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test')
    angular.mock.inject(function(EntityTestSuiteMixin, ServerReplyMixin) {
      Object.assign(this, EntityTestSuiteMixin, ServerReplyMixin)

      this.injectDependencies('$httpBackend',
                              '$httpParamSerializer',
                              'EntityName',
                              'CollectionEventTypeName',
                              'Factory')
      this.url = (...paths) => {
        const args = [ 'studies/cetypes/names' ].concat(paths);
        return EntityTestSuiteMixin.url.apply(null, args);
      }
    })
  })

  afterEach(function() {
    this.$httpBackend.verifyNoOutstandingExpectation();
    this.$httpBackend.verifyNoOutstandingRequest();
  });

  describe('create behaviour', function() {

    var context = {};

    beforeEach(function() {
      let eventType;

      context.constructor = this.CollectionEventTypeName;
      context.createFunc  = this.CollectionEventTypeName.create;
      context.restApiUrl  = (...paths) => this.url([eventType.studyId].concat(paths));
      context.jsonFactoryFunc = () => {
        const result = this.Factory.collectionEventTypeNameDto();
        eventType = this.Factory.defaultCollectionEventType();
        return result;
      }
      context.listFunc = (options) => this.CollectionEventTypeName.list(eventType.studyId, options);
    });

    sharedBehaviour.entityNameCreateSharedBehaviour(context)

  });

  describe('list behaviour', function() {

    var context = {};

    beforeEach(function() {
      let eventType;

      context.constructor = this.CollectionEventTypeName;
      context.restApiUrl  = (...paths) => this.url([eventType.studyId].concat(paths));
      context.jsonFactoryFunc = () => {
        const result = this.Factory.collectionEventTypeNameDto();
        eventType = this.Factory.defaultCollectionEventType();
        return result;
      }
      context.listFunc = (options) => this.CollectionEventTypeName.list(eventType.studyId, options);
    });

    sharedBehaviour.entityNameListSharedBehaviour(context)

  });

})
