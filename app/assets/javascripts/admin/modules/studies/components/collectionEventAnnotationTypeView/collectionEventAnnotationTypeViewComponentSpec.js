/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import { ComponentTestSuiteMixin } from 'test/mixins/ComponentTestSuiteMixin';
import ngModule from '../../index'
import sharedBehaviour from 'test/behaviours/annotationTypeViewComponentSharedBehaviour';

describe('Component: collectionEventAnnotationTypeView', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, ComponentTestSuiteMixin);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              'notificationsService',
                              'Study',
                              'CollectionEventType',
                              'AnnotationType',
                              'Factory');

      this.createController = (study, collectionEventType, annotationType) => {
        this.CollectionEventType.get =
          jasmine.createSpy().and.returnValue(this.$q.when(collectionEventType));

        this.createControllerInternal(
          `<collection-event-annotation-type-view
             study="vm.study"
             collection-event-type="vm.collectionEventType"
             annotation-type="vm.annotationType"
           </collection-event-annotation-type-view>`,
          {
            study:               study,
            collectionEventType: collectionEventType,
            annotationType:      annotationType
          },
          'collectionEventAnnotationTypeView');
      };

      this.createEntities = () => {
        var jsonAnnotType       = this.Factory.annotationType(),
            jsonCet             = this.Factory.collectionEventType({ annotationTypes: [ jsonAnnotType] }),
            study               = this.Study.create(this.Factory.study()),
            collectionEventType = this.CollectionEventType.create(jsonCet),
            annotationType      = this.AnnotationType.create(jsonAnnotType);

        return {
          study:               study,
          collectionEventType: collectionEventType,
          annotationType:      annotationType
        };
      };
    });
  });

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

    beforeEach(function () {
      var entities = this.createEntities();

      context.entity                       = this.CollectionEventType;
      context.updateAnnotationTypeFuncName = 'updateAnnotationType';
      context.parentObject                 = entities.collectionEventType;
      context.annotationType               = entities.annotationType;
      context.createController             = () => {
        this.createController(entities.study,
                              entities.collectionEventType,
                              entities.annotationType);
      };
    });

    sharedBehaviour(context);

  });

});
