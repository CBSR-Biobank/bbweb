/**
 * Jasmine test suite
 */
/* global angular */

import _ from 'lodash';
import ngModule from '../../index'
import sharedBehaviour from '../../../../../test/behaviours/annotationTypeAddComponentSharedBehaviour';

describe('Component: collectionEventAnnotationTypeAdd', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function(ComponentTestSuiteMixin) {
      _.extend(this, ComponentTestSuiteMixin);
      this.injectDependencies('$rootScope',
                              '$compile',
                              'Study',
                              'CollectionEventType',
                              'AnnotationType',
                              'Factory');

      this.study = this.Study.create(this.Factory.study());
      this.collectionEventType = this.CollectionEventType.create(this.Factory.collectionEventType());

      this.createController = () =>
        ComponentTestSuiteMixin.createController.call(
          this,
            `<collection-event-annotation-type-add
               study="vm.study"
               collection-event-type="vm.ceventType">
             </collection-event-annotation-type-add>`,
          {
            study:      this.study,
            ceventType: this.collectionEventType
          },
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

    sharedBehaviour(context);
  });

});
