/**
 * Jasmine test suite
 */
define(function(require) {
  'use strict';

  var angular                              = require('angular'),
      mocks                                = require('angularMocks'),
      _                                    = require('lodash'),
      annotationTypeAddDirectiveSharedSpec = require('../../../../directives/annotationTypeAddDirectiveSharedSpec');

  describe('Directive: collectionEventAnnotationTypeAddDirective', function() {

    var createController = function () {
      this.element = angular.element([
        '<collection-event-annotation-type-add',
        '  collection-event-type="vm.ceventType">',
        '</collection-event-annotation-type-add>'
      ].join(''));

      this.scope = this.$rootScope.$new();
      this.scope.vm = { ceventType: this.collectionEventType };
      this.$compile(this.element)(this.scope);
      this.scope.$digest();
      this.controller = this.element.controller('collectionEventAnnotationTypeAdd');
    };

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(testSuiteMixin, testUtils) {
      var self = this;

      _.extend(self, testSuiteMixin);
      self.injectDependencies('$rootScope',
                              '$compile',
                              'CollectionEventType',
                              'AnnotationType',
                              'factory');

      self.collectionEventType = new self.CollectionEventType(
        self.factory.collectionEventType(self.factory.study()));

      self.putHtmlTemplates(
        '/assets/javascripts/admin/studies/directives/annotationTypes/collectionEventAnnotationTypeAdd/collectionEventAnnotationTypeAdd.html',
        '/assets/javascripts/admin/directives/annotationTypeAdd/annotationTypeAdd.html');
    }));

    it('should have  valid scope', function() {
      createController.call(this);
      expect(this.controller.collectionEventType).toBe(this.collectionEventType);
    });

    describe('for onSubmit and onCancel', function () {
      var context = {};

      beforeEach(inject(function () {
        context.createController          = createController;
        context.scope                     = this.scope;
        context.controller                = this.controller;
        context.entity                    = this.CollectionEventType;
        context.addAnnotationTypeFuncName = 'addAnnotationType';
        context.returnState               = 'home.admin.studies.study.collection.ceventType';
      }));

      annotationTypeAddDirectiveSharedSpec(context);
    });

  });

});
