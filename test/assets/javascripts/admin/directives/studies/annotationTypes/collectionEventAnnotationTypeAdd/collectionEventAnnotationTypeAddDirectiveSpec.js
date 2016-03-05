/**
 * Jasmine test suite
 */
define([
  'angular',
  'angularMocks',
  'underscore',
  '../annotationTypeAddDirectiveSharedSpec'
], function(angular, mocks, _, annotationTypeAddDirectiveSharedSpec) {
  'use strict';

  describe('Directive: collectionEventAnnotationTypeAddDirective', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($rootScope, $compile, testUtils) {
      var self = this;

      self.CollectionEventType = self.$injector.get('CollectionEventType');
      self.AnnotationType      = self.$injector.get('AnnotationType');
      self.jsonEntities        = self.$injector.get('jsonEntities');

      self.collectionEventType = new self.CollectionEventType(
        self.jsonEntities.collectionEventType(self.jsonEntities.study()));
      self.createController = setupController();

      testUtils.putHtmlTemplates(
        '/assets/javascripts/admin/directives/studies/annotationTypes/annotationTypeAdd/annotationTypeAdd.html');

      function setupController() {
        return create;

        function create() {
          self.element = angular.element([
            '<collection-event-annotation-type-add',
            '  collection-event-type="vm.ceventType">',
            '</collection-event-annotation-type-add>'
          ].join(''));

          self.scope = $rootScope.$new();
          self.scope.vm = { ceventType: self.collectionEventType };
          $compile(self.element)(self.scope);
          self.scope.$digest();
          self.controller = self.element.controller('collectionEventAnnotationTypeAdd');
        }
      }
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
        context.returnState               = 'home.admin.studies.study.collection.view';
      }));

      annotationTypeAddDirectiveSharedSpec(context);
    });

  });

});
