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
  '../../../annotationTypeViewDirectiveSharedSpec',
  'biobankApp'
], function(angular, mocks, _, annotationTypeViewDirectiveSharedSpec) {
  'use strict';

  describe('collectionEventAnnotationTypeViewDirective', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($rootScope, $compile, templateMixin, testUtils) {
      var self = this, jsonAnnotType;

      _.extend(self, templateMixin);

      self.$q                   = self.$injector.get('$q');
      self.notificationsService = self.$injector.get('notificationsService');
      self.CollectionEventType  = self.$injector.get('CollectionEventType');
      self.AnnotationType       = self.$injector.get('AnnotationType');
      self.factory         = self.$injector.get('factory');

      jsonAnnotType = self.factory.annotationType();
      self.jsonStudy     = this.factory.study();
      self.jsonCet       = self.factory.collectionEventType(self.jsonStudy);
      self.collectionEventType =
        new self.CollectionEventType(_.extend({},
                                              self.jsonCet,
                                              { annotationTypes: [ jsonAnnotType] }));
      self.annotationType = new self.AnnotationType(jsonAnnotType);
      self.createController = createController;

      self.putHtmlTemplates(
        '/assets/javascripts/admin/directives/studies/annotationTypes/collectionEventAnnotationTypeView/collectionEventAnnotationTypeView.html',
        '/assets/javascripts/admin/directives/annotationTypeView/annotationTypeView.html',
        '/assets/javascripts/common/directives/truncateToggle.html');

      function createController() {
        self.element = angular.element([
          '<collection-event-annotation-type-view',
          '  collection-event-type="vm.collectionEventType"',
          '  annotation-type="vm.annotationType"',
          '</collection-event-annotation-type-view>'
        ].join(''));

        self.scope = $rootScope.$new();
        self.scope.vm = {
          collectionEventType: self.collectionEventType,
          annotationType:      self.annotationType
        };
        $compile(self.element)(self.scope);
        self.scope.$digest();
        self.controller = self.element.controller('collectionEventAnnotationTypeView');
      }
    }));

    it('should have  valid scope', function() {
      this.createController();
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
        context.createController             = this.createController;
      }));

      annotationTypeViewDirectiveSharedSpec(context);

    });

  });

});
