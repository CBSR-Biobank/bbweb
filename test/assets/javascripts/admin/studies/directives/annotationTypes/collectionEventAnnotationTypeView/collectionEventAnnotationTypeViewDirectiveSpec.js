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

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(TestSuiteMixin) {
      var self = this;

      _.extend(self, TestSuiteMixin.prototype);

      self.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              'notificationsService',
                              'Study',
                              'CollectionEventType',
                              'AnnotationType',
                              'factory');

      self.putHtmlTemplates(
        '/assets/javascripts/admin/studies/directives/annotationTypes/collectionEventAnnotationTypeView/collectionEventAnnotationTypeView.html',
        '/assets/javascripts/admin/directives/annotationTypeView/annotationTypeView.html',
        '/assets/javascripts/common/directives/truncateToggle/truncateToggle.html',
        '/assets/javascripts/common/components/breadcrumbs/breadcrumbs.html');

      this.createController = function (study, collectionEventType, annotationType) {
        this.CollectionEventType.get =
          jasmine.createSpy().and.returnValue(this.$q.when(collectionEventType));

        this.element = angular.element([
          '<collection-event-annotation-type-view',
          '  study="vm.study"',
          '  collection-event-type="vm.collectionEventType"',
          '  annotation-type="vm.annotationType"',
          '</collection-event-annotation-type-view>'
        ].join(''));

        this.scope = this.$rootScope.$new();
        this.scope.vm = {
          study:               study,
          collectionEventType: collectionEventType,
          annotationType:      annotationType
        };
        this.$compile(this.element)(this.scope);
        this.scope.$digest();
        this.controller = this.element.controller('collectionEventAnnotationTypeView');
      };

      this.createEntities = function() {
        var jsonAnnotType = self.factory.annotationType(),
            jsonStudy     = self.factory.study(),
            jsonCet       = self.factory.collectionEventType(jsonStudy),
            study         = self.Study.create(jsonStudy),
            collectionEventType = self.CollectionEventType.create(
              _.extend({}, jsonCet, { annotationTypes: [ jsonAnnotType] })),
            annotationType = new self.AnnotationType(jsonAnnotType);

        return {
          study:               study,
          collectionEventType: collectionEventType,
          annotationType:      annotationType
        };
      };
    }));

    it('should have  valid scope', function() {
      var entities = this.createEntities();

      this.createController(entities.study,
                            entities.collectionEventType,
                            entities.annotationType);
      expect(this.controller.study).toBe(entities.study);
      expect(this.controller.collectionEventType).toBe(entities.collectionEventType);
      expect(this.controller.annotationType).toBe(entities.annotationType);
    });

    describe('shared behaviour', function () {
      var context = {};

      beforeEach(inject(function () {
        var self = this, entities = self.createEntities();

        context.entity                       = this.CollectionEventType;
        context.updateAnnotationTypeFuncName = 'updateAnnotationType';
        context.parentObject                 = entities.collectionEventType;
        context.annotationType               = entities.annotationType;
        context.createController             = function () {
          self.createController(entities.study,
                                entities.collectionEventType,
                                entities.annotationType);
        };
      }));

      annotationTypeViewDirectiveSharedSpec(context);

    });

  });

});
