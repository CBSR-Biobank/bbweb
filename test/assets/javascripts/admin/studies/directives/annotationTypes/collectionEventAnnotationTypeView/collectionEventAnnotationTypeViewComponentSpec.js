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

  describe('Component: collectionEventAnnotationTypeView', function() {

    function SuiteMixinFactory(ComponentTestSuiteMixin) {

      function SuiteMixin() {
      }

      SuiteMixin.prototype = Object.create(ComponentTestSuiteMixin.prototype);
      SuiteMixin.prototype.constructor = SuiteMixin;

      SuiteMixin.prototype.createController = function (study, collectionEventType, annotationType) {
        this.CollectionEventType.get =
          jasmine.createSpy().and.returnValue(this.$q.when(collectionEventType));

        ComponentTestSuiteMixin.prototype.createController.call(
          this,
          [
            '<collection-event-annotation-type-view',
            '  study="vm.study"',
            '  collection-event-type="vm.collectionEventType"',
            '  annotation-type="vm.annotationType"',
            '</collection-event-annotation-type-view>'
          ].join(''),
          {
            study:               study,
            collectionEventType: collectionEventType,
            annotationType:      annotationType
          },
          'collectionEventAnnotationTypeView');
      };

      SuiteMixin.prototype.createEntities = function() {
        var jsonAnnotType       = this.factory.annotationType(),
            jsonStudy           = this.factory.study(),
            jsonCet             = this.factory.collectionEventType(jsonStudy),
            study               = this.Study.create(jsonStudy),
            collectionEventType = this.CollectionEventType.create(
              _.extend({}, jsonCet, { annotationTypes: [ jsonAnnotType] })),
            annotationType      = new this.AnnotationType(jsonAnnotType);

        return {
          study:               study,
          collectionEventType: collectionEventType,
          annotationType:      annotationType
        };
      };

      return SuiteMixin;
    }

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(ComponentTestSuiteMixin) {
      _.extend(this, new SuiteMixinFactory(ComponentTestSuiteMixin).prototype);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              'notificationsService',
                              'Study',
                              'CollectionEventType',
                              'AnnotationType',
                              'factory');

      this.putHtmlTemplates(
        '/assets/javascripts/admin/studies/components/annotationTypes/collectionEventAnnotationTypeView/collectionEventAnnotationTypeView.html',
        '/assets/javascripts/admin/components/annotationTypeView/annotationTypeView.html',
        '/assets/javascripts/common/directives/truncateToggle/truncateToggle.html',
        '/assets/javascripts/common/components/breadcrumbs/breadcrumbs.html');
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

      annotationTypeViewComponentSharedSpec(context);

    });

  });

});
