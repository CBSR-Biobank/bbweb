/**
 * Jasmine test suite
 */
/* global angular */

import _ from 'lodash';
import annotationTypeAddComponentSharedSpec from '../../../../../test/behaviours/annotationTypeAddComponentSharedSpec.js';

describe('Component: collectionEventAnnotationTypeAdd', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(ComponentTestSuiteMixin) {
      _.extend(this, ComponentTestSuiteMixin);
      this.injectDependencies('$rootScope',
                              '$compile',
                              'CollectionEventType',
                              'AnnotationType',
                              'Factory');

      this.collectionEventType = new this.CollectionEventType(
        this.Factory.collectionEventType(this.Factory.study()));

      this.createController = () =>
        ComponentTestSuiteMixin.createController.call(
          this,
            `<collection-event-annotation-type-add
               study="vm.study"
               collection-event-type="vm.ceventType">
             </collection-event-annotation-type-add>`,
          { ceventType: this.collectionEventType },
          'collectionEventAnnotationTypeAdd');
    });
  });

  it('should have  valid scope', function() {
    this.createController();
    expect(this.controller.collectionEventType).toBe(this.collectionEventType);
  });

  describe('for onSubmit and onCancel', function () {
    var context = {};

    beforeEach(function () {
      context.createController          = this.createController;
      context.scope                     = this.scope;
      context.controller                = this.controller;
      context.entity                    = this.CollectionEventType;
      context.addAnnotationTypeFuncName = 'addAnnotationType';
      context.returnState               = 'home.admin.studies.study.collection.ceventType';
    });

    annotationTypeAddComponentSharedSpec(context);
  });

});
