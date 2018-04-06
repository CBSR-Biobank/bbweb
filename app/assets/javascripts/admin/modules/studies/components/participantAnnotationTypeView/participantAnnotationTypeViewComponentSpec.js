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

describe('Component: participantAnnotationTypeView', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      var jsonAnnotType;

      Object.assign(this, ComponentTestSuiteMixin);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              'notificationsService',
                              'Study',
                              'AnnotationType',
                              'Factory');

      jsonAnnotType = this.Factory.annotationType();
      this.study = this.Study.create(Object.assign(this.Factory.study(),
                                              { annotationTypes: [ jsonAnnotType ]}));
      this.annotationType = new this.AnnotationType(jsonAnnotType);

      this.createController = () =>
        ComponentTestSuiteMixin.createController.call(
          this,
          `<participant-annotation-type-view
            study="vm.study"
            annotation-type="vm.annotationType"
          </participant-annotation-type-view>`,
          {
            study:          this.study,
            annotationType: this.annotationType
          },
          'participantAnnotationTypeView');
    });
  });

  it('should have  valid scope', function() {
    this.createController();
    expect(this.controller.study).toBe(this.study);
    expect(this.controller.annotationType).toBe(this.annotationType);
  });

  describe('shared behaviour', function () {
    var context = {};

    beforeEach(function () {
      context.entity                       = this.Study;
      context.updateAnnotationTypeFuncName = 'updateAnnotationType';
      context.parentObject                 = this.study;
      context.annotationType               = this.annotationType;
      context.createController             = this.createController;
    });

    sharedBehaviour(context);

  });

});
