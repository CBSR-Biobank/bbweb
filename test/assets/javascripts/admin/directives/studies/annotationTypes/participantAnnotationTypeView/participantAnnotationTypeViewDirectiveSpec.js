/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'underscore',
  '../annotationTypeViewDirectiveSharedSpec',
  'biobankApp'
], function(angular, mocks, _, annotationTypeViewDirectiveSharedSpec) {
  'use strict';

  describe('participantAnnotationTypeViewDirective', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($rootScope, $compile, directiveTestSuite, testUtils) {
      var self = this, jsonAnnotType;

      _.extend(self, directiveTestSuite);

      self.$q                   = self.$injector.get('$q');
      self.notificationsService = self.$injector.get('notificationsService');
      self.Study                = self.$injector.get('Study');
      self.AnnotationType       = self.$injector.get('AnnotationType');
      self.jsonEntities         = self.$injector.get('jsonEntities');

      jsonAnnotType = self.jsonEntities.annotationType();
      self.study = new self.Study(_.extend(self.jsonEntities.study(),
                                           { annotationTypes: [ jsonAnnotType ]}));
      self.annotationType = new self.AnnotationType(jsonAnnotType);
      self.createController = createController;

      self.putHtmlTemplates(
        '/assets/javascripts/admin/directives/studies/annotationTypes/participantAnnotationTypeView/participantAnnotationTypeView.html',
        '/assets/javascripts/admin/directives/studies/annotationTypes/annotationTypeView/annotationTypeView.html',
        '/assets/javascripts/common/directives/truncateToggle.html');

      function createController() {
        self.element = angular.element([
          '<participant-annotation-type-view',
          '  study="vm.study"',
          '  annotation-type="vm.annotationType"',
          '</participant-annotation-type-view>'
        ].join(''));

        self.scope = $rootScope.$new();
        self.scope.vm = {
          study:          self.study,
          annotationType: self.annotationType
        };
        $compile(self.element)(self.scope);
        self.scope.$digest();
        self.controller = self.element.controller('participantAnnotationTypeView');
      }
    }));

    it('should have  valid scope', function() {
      this.createController();
      expect(this.controller.study).toBe(this.study);
      expect(this.controller.annotationType).toBe(this.annotationType);
    });

    describe('shared behaviour', function () {
      var context = {};

      beforeEach(inject(function () {
        context.entity                       = this.Study;
        context.updateAnnotationTypeFuncName = 'updateAnnotationType';
        context.parentObject                 = this.study;
        context.annotationType               = this.annotationType;
        context.createController             = this.createController;
      }));

      annotationTypeViewDirectiveSharedSpec(context);

    });

  });

});
