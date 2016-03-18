/**
 * Jasmine test suite
 */
define(function(require) {
  'use strict';

  var angular                              = require('angular'),
      mocks                                = require('angularMocks'),
      _                                    = require('underscore'),
      annotationTypeAddDirectiveSharedSpec = require('../annotationTypeAddDirectiveSharedSpec');

  describe('Directive: collectionEventAnnotationTypeAddDirective', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($rootScope, $compile, directiveTestSuite, testUtils) {
      var self = this;

      _.extend(self, directiveTestSuite);

      self.CollectionEventType = self.$injector.get('CollectionEventType');
      self.AnnotationType      = self.$injector.get('AnnotationType');
      self.jsonEntities        = self.$injector.get('jsonEntities');

      self.collectionEventType = new self.CollectionEventType(
        self.jsonEntities.collectionEventType(self.jsonEntities.study()));
      self.createController = createController;

      self.putHtmlTemplates(
        '/assets/javascripts/admin/directives/studies/annotationTypes/annotationTypeAdd/annotationTypeAdd.html',
        '/assets/javascripts/admin/directives/studies/annotationTypes/collectionEventAnnotationTypeAdd/collectionEventAnnotationTypeAdd.html');

      function createController() {
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

      annotationTypeAddDirectiveSharedSpec(context);
    });

  });

});
