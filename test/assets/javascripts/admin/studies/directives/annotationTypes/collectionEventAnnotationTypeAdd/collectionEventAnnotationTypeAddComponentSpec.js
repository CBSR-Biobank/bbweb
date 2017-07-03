/**
 * Jasmine test suite
 */
define(function(require) {
  'use strict';

  var angular                              = require('angular'),
      mocks                                = require('angularMocks'),
      _                                    = require('lodash'),
      annotationTypeAddComponentSharedSpec = require('../../../../../test/annotationTypeAddComponentSharedSpec');

  describe('Component: collectionEventAnnotationTypeAdd', function() {

    function SuiteMixinFactory(ComponentTestSuiteMixin) {

      function SuiteMixin() {
      }

      SuiteMixin.prototype = Object.create(ComponentTestSuiteMixin.prototype);
      SuiteMixin.prototype.constructor = SuiteMixin;

      SuiteMixin.prototype.createController = function () {
        ComponentTestSuiteMixin.prototype.createController.call(
          this,
          [
            '<collection-event-annotation-type-add',
            '  collection-event-type="vm.ceventType">',
            '</collection-event-annotation-type-add>'
          ].join(''),
          { ceventType: this.collectionEventType },
          'collectionEventAnnotationTypeAdd');
      };

      return SuiteMixin;
    }

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(ComponentTestSuiteMixin) {
      _.extend(this, new SuiteMixinFactory(ComponentTestSuiteMixin).prototype);
      this.injectDependencies('$rootScope',
                              '$compile',
                              'CollectionEventType',
                              'AnnotationType',
                              'factory');

      this.collectionEventType = new this.CollectionEventType(
        this.factory.collectionEventType(this.factory.study()));

      this.putHtmlTemplates(
        '/assets/javascripts/admin/studies/components/annotationTypes/collectionEventAnnotationTypeAdd/collectionEventAnnotationTypeAdd.html',
        '/assets/javascripts/admin/components/annotationTypeAdd/annotationTypeAdd.html');
    }));

    it('should have  valid scope', function() {
      this.createController();
      expect(this.controller.collectionEventType).toBe(this.collectionEventType);
    });

    describe('for onSubmit and onCancel', function () {
      var context = {};

      beforeEach(inject(function () {
        context.createController          = this.createController;
        context.scope                     = this.scope;
        context.controller                = this.controller;
        context.entity                    = this.CollectionEventType;
        context.addAnnotationTypeFuncName = 'addAnnotationType';
        context.returnState               = 'home.admin.studies.study.collection.ceventType';
      }));

      annotationTypeAddComponentSharedSpec(context);
    });

  });

});
