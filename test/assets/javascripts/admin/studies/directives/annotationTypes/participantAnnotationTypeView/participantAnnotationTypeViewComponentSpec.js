/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function(require) {
  'use strict';

  var mocks                                 = require('angularMocks'),
      _                                     = require('lodash'),
      annotationTypeViewComponentSharedSpec = require('../../../../../test/annotationTypeViewComponentSharedSpec');

  describe('Component: participantAnnotationTypeView', function() {

    function SuiteMixinFactory(ComponentTestSuiteMixin) {

      function SuiteMixin() {
      }

      SuiteMixin.prototype = Object.create(ComponentTestSuiteMixin.prototype);
      SuiteMixin.prototype.constructor = SuiteMixin;

      SuiteMixin.prototype.createController = function () {
        ComponentTestSuiteMixin.prototype.createController.call(
          this,
          [
            '<participant-annotation-type-view',
            '  study="vm.study"',
            '  annotation-type="vm.annotationType"',
            '</participant-annotation-type-view>'
          ].join(''),{
            study:          this.study,
            annotationType: this.annotationType
          },
          'participantAnnotationTypeView');
      };

      return SuiteMixin;
    }

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(ComponentTestSuiteMixin) {
      var jsonAnnotType;

      _.extend(this, new SuiteMixinFactory(ComponentTestSuiteMixin).prototype);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              'notificationsService',
                              'Study',
                              'AnnotationType',
                              'factory');

      jsonAnnotType = this.factory.annotationType();
      this.study = this.Study.create(_.extend(this.factory.study(),
                                           { annotationTypes: [ jsonAnnotType ]}));
      this.annotationType = new this.AnnotationType(jsonAnnotType);

      this.putHtmlTemplates(
        '/assets/javascripts/admin/studies/components/annotationTypes/participantAnnotationTypeView/participantAnnotationTypeView.html',
        '/assets/javascripts/admin/components/annotationTypeView/annotationTypeView.html',
        '/assets/javascripts/common/directives/truncateToggle/truncateToggle.html',
        '/assets/javascripts/common/components/breadcrumbs/breadcrumbs.html');
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

      annotationTypeViewComponentSharedSpec(context);

    });

  });

});
