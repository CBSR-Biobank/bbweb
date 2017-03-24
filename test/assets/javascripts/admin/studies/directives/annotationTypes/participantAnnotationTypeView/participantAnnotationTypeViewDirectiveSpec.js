/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'lodash',
  '../../../../directives/annotationTypeViewDirectiveSharedSpec',
  'biobankApp'
], function(angular, mocks, _, annotationTypeViewDirectiveSharedSpec) {
  'use strict';

  describe('participantAnnotationTypeViewDirective', function() {

    var createController = function () {
      this.element = angular.element([
        '<participant-annotation-type-view',
        '  study="vm.study"',
        '  annotation-type="vm.annotationType"',
        '</participant-annotation-type-view>'
      ].join(''));

      this.scope = this.$rootScope.$new();
      this.scope.vm = {
        study:          this.study,
        annotationType: this.annotationType
      };
      this.$compile(this.element)(this.scope);
      this.scope.$digest();
      this.controller = this.element.controller('participantAnnotationTypeView');
    };

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($rootScope, $compile, TestSuiteMixin) {
      var self = this, jsonAnnotType;

      _.extend(self, TestSuiteMixin.prototype);

      self.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              'notificationsService',
                              'Study',
                              'AnnotationType',
                              'factory');

      jsonAnnotType = self.factory.annotationType();
      self.study = self.Study.create(_.extend(self.factory.study(),
                                           { annotationTypes: [ jsonAnnotType ]}));
      self.annotationType = new self.AnnotationType(jsonAnnotType);

      self.putHtmlTemplates(
        '/assets/javascripts/admin/studies/directives/annotationTypes/participantAnnotationTypeView/participantAnnotationTypeView.html',
        '/assets/javascripts/admin/directives/annotationTypeView/annotationTypeView.html',
        '/assets/javascripts/common/directives/truncateToggle/truncateToggle.html');
    }));

    it('should have  valid scope', function() {
      createController.call(this);
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
        context.createController             = createController;
      }));

      annotationTypeViewDirectiveSharedSpec(context);

    });

  });

});
