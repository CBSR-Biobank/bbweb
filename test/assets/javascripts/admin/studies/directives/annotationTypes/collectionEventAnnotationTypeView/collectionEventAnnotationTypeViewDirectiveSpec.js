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

  describe('collectionEventAnnotationTypeViewDirective', function() {

    var createController = function () {
      this.element = angular.element([
        '<collection-event-annotation-type-view',
        '  collection-event-type="vm.collectionEventType"',
        '  annotation-type="vm.annotationType"',
        '</collection-event-annotation-type-view>'
      ].join(''));

      this.scope = this.$rootScope.$new();
      this.scope.vm = {
        collectionEventType: this.collectionEventType,
        annotationType:      this.annotationType
      };
      this.$compile(this.element)(this.scope);
      this.scope.$digest();
      this.controller = this.element.controller('collectionEventAnnotationTypeView');
    };

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(TestSuiteMixin, testUtils) {
      var self = this, jsonAnnotType;

      _.extend(self, TestSuiteMixin.prototype);

      self.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              'notificationsService',
                              'CollectionEventType',
                              'AnnotationType',
                              'factory');

      jsonAnnotType = self.factory.annotationType();
      self.jsonStudy     = this.factory.study();
      self.jsonCet       = self.factory.collectionEventType(self.jsonStudy);
      self.collectionEventType =
        new self.CollectionEventType(_.extend({},
                                              self.jsonCet,
                                              { annotationTypes: [ jsonAnnotType] }));
      self.annotationType = new self.AnnotationType(jsonAnnotType);

      self.putHtmlTemplates(
        '/assets/javascripts/admin/studies/directives/annotationTypes/collectionEventAnnotationTypeView/collectionEventAnnotationTypeView.html',
        '/assets/javascripts/admin/directives/annotationTypeView/annotationTypeView.html',
        '/assets/javascripts/common/directives/truncateToggle/truncateToggle.html');

    }));

    it('should have  valid scope', function() {
      createController.call(this);
      expect(this.controller.collectionEventType).toBe(this.collectionEventType);
      expect(this.controller.annotationType).toBe(this.annotationType);
    });

    describe('shared behaviour', function () {
      var context = {};

      beforeEach(inject(function () {
        context.entity                       = this.CollectionEventType;
        context.updateAnnotationTypeFuncName = 'updateAnnotationType';
        context.parentObject                 = this.collectionEventType;
        context.annotationType               = this.annotationType;
        context.createController             = createController;
      }));

      annotationTypeViewDirectiveSharedSpec(context);

    });

  });

});
