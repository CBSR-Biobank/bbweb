/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import { ComponentTestSuiteMixin } from 'test/mixins/ComponentTestSuiteMixin';
import _ from 'lodash';
import ngModule from '../../index'

describe('Component: participantGet', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, ComponentTestSuiteMixin);

      this.injectDependencies('$q',
                              '$log',
                              '$rootScope',
                              '$compile',
                              '$state',
                              'modalService',
                              'Study',
                              'Participant',
                              'Factory');

      this.jsonParticipant = this.Factory.participant();
      this.jsonStudy       = this.Factory.defaultStudy();
      this.participant     = this.Participant.create(this.jsonParticipant);
      this.study           = this.Study.create(this.jsonStudy);

      this.createController = (study) => {
        study = study || this.study;

        this.createControllerInternal(
          '<participant-get study="vm.study"></participant-get>',
          { study: study },
          'participantGet');
      };

      this.createStudies = (numStudies) =>
        _.range(numStudies).map(() => this.Study.create(self.Factory.study()));

      this.createGetStudiesFn = (studies) =>
        (pagerOptions) =>  this.$q.when(Object.assign(this.Factory.pagedResult(studies, pagerOptions),
                                                { items: studies.slice(0, pagerOptions.limit) }));
    });
  });

  it('has valid scope', function() {
    this.createController();
    expect(this.controller).toBeDefined();
    expect(this.controller.study).toBe(this.study);
    expect(this.controller.onSubmit).toBeFunction();
  });

  describe('when invoking uniqueIdChanged', function() {

    it('does nothing with an empty participant ID', function() {
      this.Participant.get = jasmine.createSpy().and.returnValue(this.$q.when(this.participant));
      this.createController();
      this.controller.uniqueId = '';
      this.controller.onSubmit();
      this.scope.$digest();

      expect(this.Participant.get).not.toHaveBeenCalled();
    });

    it('with a valid participant ID changes state', function() {
      this.Participant.get = jasmine.createSpy().and.returnValue(this.$q.when(this.participant));
      spyOn(this.$state, 'go').and.returnValue(null);

      this.createController();
      this.controller.uniqueId = this.participant.uniqueId;
      this.controller.onSubmit();
      this.scope.$digest();

      expect(this.$state.go).toHaveBeenCalledWith('home.collection.study.participant.summary',
                                                  { participantSlug: this.participant.slug });
    });

    it('with a NOT_FOUND opens a modal', function() {
      var uniqueId = this.Factory.stringNext(),
          errorMsg = {
            status:  'error',
            message: 'EntityCriteriaNotFound: participant slug'
          };

      this.createController();

      this.Participant.get = jasmine.createSpy().and.returnValue(this.$q.reject(errorMsg));
      spyOn(this.modalService, 'modalOkCancel').and.returnValue(this.$q.when('ok'));
      spyOn(this.$state, 'go').and.returnValue('ok');

      this.controller.uniqueId = uniqueId;
      this.controller.onSubmit();
      this.scope.$digest();

      expect(this.modalService.modalOkCancel).toHaveBeenCalled();
      expect(this.$state.go).toHaveBeenCalledWith('home.collection.study.participantAdd',
                                                  { uniqueId: uniqueId });
    });

    it('with a NOT_FOUND opens a modal and cancel is pressed', function() {
      var uniqueId = this.Factory.stringNext(),
          errorMsg = {
            status:  'error',
            message: 'EntityCriteriaNotFound: participant slug'
          };

      this.createController();

      this.Participant.get = jasmine.createSpy().and.returnValue(this.$q.reject(errorMsg));
      spyOn(this.modalService, 'modalOkCancel').and.returnValue(this.$q.reject('Cancel'));
      spyOn(this.$state, 'reload').and.returnValue(null);

      this.controller.uniqueId = uniqueId;
      this.controller.onSubmit();
      this.scope.$digest();

      expect(this.modalService.modalOkCancel).toHaveBeenCalled();
      expect(this.$state.reload).toHaveBeenCalled();
    });

    it('on a 404 response, when patient with unique id already exists, modal is shown to user', function() {
      this.createController();
      this.Participant.get = jasmine.createSpy().and.returnValue(
        this.$q.reject({
          status:  'error',
          message: 'EntityCriteriaError: participant not in study'
        }));
      spyOn(this.modalService, 'modalOk').and.returnValue(this.$q.when('Ok'));

      this.controller.uniqueId = this.Factory.stringNext();
      this.controller.onSubmit();
      this.scope.$digest();

      expect(this.modalService.modalOk).toHaveBeenCalled();
    });

    it('promise is rejected on a non 404 response', function() {
      this.createController();
      this.Participant.get = jasmine.createSpy().and.returnValue(this.$q.reject(
        { status: 400, data: { message: 'xxx' } }));
      spyOn(this.$log, 'error').and.callThrough();

      this.controller.uniqueId = this.Factory.stringNext();
      this.controller.onSubmit();
      this.scope.$digest();

      expect(this.$log.error).toHaveBeenCalled();
    });

  });

});
