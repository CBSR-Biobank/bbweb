/**
 * Jasmine test suite
 */
define([
  'angular',
  'angularMocks',
  'underscore',
  '../../../annotationTypeAddDirectiveSharedSpec'
], function(angular, mocks, _, annotationTypeAddDirectiveSharedSpec) {
  'use strict';

  describe('Directive: participantAnnotationTypeAddDirective', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($rootScope, $compile, directiveTestSuite, testUtils) {
      var self = this;

      _.extend(self, directiveTestSuite);

      self.Study          = self.$injector.get('Study');
      self.jsonEntities   = self.$injector.get('jsonEntities');

      self.study = new self.Study(self.jsonEntities.study());
      self.createController = createController;

      self.putHtmlTemplates(
        '/assets/javascripts/admin/directives/studies/annotationTypes/participantAnnotationTypeAdd/participantAnnotationTypeAdd.html',
        '/assets/javascripts/admin/directives/annotationTypeAdd/annotationTypeAdd.html');

      //---

      function createController() {
        self.element = angular.element([
          '<participant-annotation-type-add',
          '  study="vm.study"',
          '</participant-annotation-type-add>'
        ].join(''));

        self.scope = $rootScope.$new();
        self.scope.vm = { study: self.study };
        $compile(self.element)(self.scope);
        self.scope.$digest();
        self.controller = self.element.controller('participantAnnotationTypeAdd');
      }
    }));

    it('should have  valid scope', function() {
      this.createController();
      expect(this.controller.study).toBe(this.study);
    });

    describe('for onSubmit and onCancel', function () {
      var context = {};

      beforeEach(inject(function () {
        context.createController          = this.createController;
        context.scope                     = this.scope;
        context.controller                = this.controller;
        context.entity                    = this.Study;
        context.addAnnotationTypeFuncName = 'addAnnotationType';
        context.returnState               = 'home.admin.studies.study.participants';
      }));

      annotationTypeAddDirectiveSharedSpec(context);
    });

  });

});
