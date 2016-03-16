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

  describe('collectionEventAnnotationTypeViewDirective', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($rootScope, $compile, directiveTestSuite, testUtils) {
      var self = this, jsonAnnotType;

      _.extend(self, directiveTestSuite);

      self.$q                   = self.$injector.get('$q');
      self.notificationsService = self.$injector.get('notificationsService');
      self.CollectionEventType  = self.$injector.get('CollectionEventType');
      self.AnnotationType       = self.$injector.get('AnnotationType');
      self.jsonEntities         = self.$injector.get('jsonEntities');

      jsonAnnotType = self.jsonEntities.annotationType();
      self.jsonStudy     = this.jsonEntities.study();
      self.jsonCet       = self.jsonEntities.collectionEventType(self.jsonStudy);
      self.collectionEventType =
        new self.CollectionEventType(_.extend({},
                                              self.jsonCet,
                                              { annotationTypes: [ jsonAnnotType] }));
      self.annotationType = new self.AnnotationType(jsonAnnotType);
      self.createController = setupController();

      self.putHtmlTemplates(
        '/assets/javascripts/admin/directives/studies/annotationTypes/collectionEventAnnotationTypeView/collectionEventAnnotationTypeView.html',
        '/assets/javascripts/admin/directives/studies/annotationTypes/annotationTypeView/annotationTypeView.html',
        '/assets/javascripts/common/directives/truncateToggle.html');

      function setupController() {
        return create;

        function create() {
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
