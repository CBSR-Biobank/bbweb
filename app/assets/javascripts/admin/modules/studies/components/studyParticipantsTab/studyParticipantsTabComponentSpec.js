/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';
import ngModule from '../../index'

describe('Component: studyParticipantsTab', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function(ComponentTestSuiteMixin) {
      _.extend(this, ComponentTestSuiteMixin);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              '$state',
                              'domainNotificationService',
                              'modalService',
                              'Study',
                              'AnnotationType',
                              'Factory');

      this.jsonStudy = this.Factory.study();
      this.study     = new this.Study(this.jsonStudy);

      spyOn(this.$state, 'go').and.returnValue('ok');

      this.createScope = () => {
        var scope = ComponentTestSuiteMixin.createScope.call(this, { study: this.study });
        this.eventRxFunc = jasmine.createSpy().and.returnValue(null);
        scope.$on('tabbed-page-update', this.eventRxFunc);
        return scope;
      };

      this.createController = () => {
        ComponentTestSuiteMixin.createController.call(
          this,
          '<study-participants-tab study="vm.study"></study-participants-tab>',
          { study: this.study },
          'studyParticipantsTab');

      };
    });
  });

  it('initialization is valid', function() {
    this.createController();

    expect(this.controller.study).toBe(this.study);
    expect(this.controller.add).toBeFunction();
    expect(this.controller.editAnnotationType).toBeFunction();
    expect(this.controller.removeAnnotationType).toBeFunction();
    expect(this.eventRxFunc).toHaveBeenCalled();
  });

  it('invoking add changes state', function() {
    this.createController();

    this.controller.add();
    this.scope.$digest();

    expect(this.$state.go).toHaveBeenCalledWith(
      'home.admin.studies.study.participants.annotationTypeAdd');
  });

  describe('for annotation types', function() {

    beforeEach(function() {
      this.annotationType = new this.AnnotationType(this.Factory.annotationType());
    });

    it('invoking editAnnotationType changes state', function() {
      this.createController();

      this.controller.editAnnotationType(this.annotationType);
      this.scope.$digest();

      expect(this.$state.go).toHaveBeenCalledWith(
        'home.admin.studies.study.participants.annotationTypeView',
        { annotationTypeSlug: this.annotationType.slug });
    });

    describe('when removing an annotation type', function() {

      it('removes the annotation type from the study when valid conditions met', function() {
        spyOn(this.modalService, 'modalOkCancel').and.returnValue(this.$q.when('OK'));
        spyOn(this.Study.prototype, 'removeAnnotationType')
          .and.returnValue(this.$q.when(this.study));

        this.createController();
        this.controller.removeAnnotationType(this.annotationType);
        this.scope.$digest();

        expect(this.Study.prototype.removeAnnotationType).toHaveBeenCalled();
      });

      it('displays a modal when it cant be removed', function() {
        spyOn(this.modalService, 'modalOkCancel').and.returnValue(this.$q.when('OK'));

        this.createController();

        this.controller.annotationTypeIdsInUse = [ this.annotationType.uniqueId ];
        this.controller.removeAnnotationType(this.annotationType);
        this.scope.$digest();

        expect(this.modalService.modalOkCancel).toHaveBeenCalled();
      });

      it('throws an error when it cant be removed', function() {
        var self = this;

        this.createController();
        this.controller.annotationTypeIdsInUse = [ ];
        this.controller.modificationsAllowed = false;

        expect(function () {
          self.controller.removeAnnotationType(self.annotationType);
        }).toThrowError(/modifications not allowed/);
      });

    });

  });

});
