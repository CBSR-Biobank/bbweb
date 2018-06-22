/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import { ComponentTestSuiteMixin } from 'test/mixins/ComponentTestSuiteMixin';
import ngModule from '../../index'

describe('Component: studyParticipantsTab', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, ComponentTestSuiteMixin);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$state',
                              'domainNotificationService',
                              'modalService',
                              'Study',
                              'AnnotationType',
                              'Factory');

      this.createFixture = () => {
        const plainAnnotationType = this.Factory.annotationType();
        const plainStudy = this.Factory.study({ annotationTypes: [ plainAnnotationType ] });

        return {
          plainStudy,
          plainAnnotationType,
          study: this.Study.create(plainStudy)
        };
      };

      spyOn(this.$state, 'go').and.returnValue('ok');

      this.createController = (fixture) => {
        this.eventRxFunc = jasmine.createSpy('eventRxFunc').and.returnValue(null);
        this.$rootScope.$on('tabbed-page-update', this.eventRxFunc);

        this.createControllerInternal(
          '<study-participants-tab study="vm.study"></study-participants-tab>',
          { study: fixture.study },
          'studyParticipantsTab');
      };
    });
  });

  it('initialization is valid', function() {
    const f = this.createFixture();
    this.createController(f);

    expect(this.controller.study).toBe(f.study);
    expect(this.controller.add).toBeFunction();
    expect(this.controller.editAnnotationType).toBeFunction();
    expect(this.controller.removeAnnotationType).toBeFunction();
    expect(this.eventRxFunc).toHaveBeenCalled();
  });

  it('invoking add changes state', function() {
    const f = this.createFixture();
    this.createController(f);

    this.controller.add();
    this.scope.$digest();

    expect(this.$state.go).toHaveBeenCalledWith(
      'home.admin.studies.study.participants.annotationTypeAdd');
  });

  describe('for annotation types', function() {

    it('invoking editAnnotationType changes state', function() {
      const f = this.createFixture();
      const annotationType = f.study.annotationTypes[0];
      this.createController(f);

      this.controller.editAnnotationType(annotationType);
      this.scope.$digest();

      expect(this.$state.go).toHaveBeenCalledWith(
        'home.admin.studies.study.participants.annotationTypeView',
        { annotationTypeSlug: annotationType.slug });
    });

    describe('when removing an annotation type', function() {

      it('removes the annotation type from the study when valid conditions met', function() {
        spyOn(this.modalService, 'modalOkCancel').and.returnValue(this.$q.when('OK'));
        spyOn(this.Study.prototype, 'removeAnnotationType')
          .and.returnValue(this.$q.when(this.study));

        const f = this.createFixture();
        const annotationType = f.study.annotationTypes[0];
        this.createController(f);
        this.controller.removeAnnotationType(annotationType);
        this.scope.$digest();

        expect(this.Study.prototype.removeAnnotationType).toHaveBeenCalled();
      });

      it('displays a modal when it cant be removed', function() {
        spyOn(this.modalService, 'modalOk').and.returnValue(this.$q.when('OK'));

        const f = this.createFixture();
        const annotationType = f.study.annotationTypes[0];
        this.createController(f);
        this.controller.annotationTypeIdsInUse = [ annotationType.id ];
        this.controller.removeAnnotationType(annotationType);
        this.scope.$digest();

        expect(this.modalService.modalOk).toHaveBeenCalled();
      });

      it('throws an error when it cant be removed', function() {
        const f = this.createFixture();
        this.createController(f);
        this.controller.annotationTypeIdsInUse = [ ];
        this.controller.modificationsAllowed = false;

        expect(() => {
          this.controller.removeAnnotationType(f.study.annotationTypes[0]);
        }).toThrowError(/modifications not allowed/);
      });

    });

  });

});
