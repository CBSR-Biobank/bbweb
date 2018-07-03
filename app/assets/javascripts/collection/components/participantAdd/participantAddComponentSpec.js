/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import { ComponentTestSuiteMixin } from 'test/mixins/ComponentTestSuiteMixin';
import _ from 'lodash';
import ngModule from '../../../app'

describe('Component: participantAdd', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test', function ($qProvider) {
      // this is needed to make promis rejections work
      $qProvider.errorOnUnhandledRejections(false);
    });

    angular.mock.inject(function() {
      Object.assign(this, ComponentTestSuiteMixin);

      this.injectDependencies('$q',
                              '$httpBackend',
                              '$state',
                              'domainNotificationService',
                              'notificationsService',
                              'Study',
                              'Participant',
                              'Factory');

      this.jsonParticipant = this.Factory.participant();
      this.jsonStudy       = this.Factory.defaultStudy();
      this.study           = new this.Study(this.jsonStudy);
      this.uniqueId        = this.jsonParticipant.uniqueId;

      this.init();

      this.createController = (study, uniqueId) => {
        study = study || this.study;
        uniqueId = uniqueId || this.uniqueId;

        this.createControllerInternal(
          `<participant-add
             study="vm.study"
             unique-id="${uniqueId}">
           </participant-add>`,
          { study: study },
          'participantAdd');
      };

      this.expectStudy = (plainStudy) => {
        if (!this.studyRequestHandler) {
          this.studyRequestHandler = this.$httpBackend
            .whenGET(new RegExp('^' + ComponentTestSuiteMixin.url('studies') + '/\\w+$'));
        }
        this.studyRequestHandler.respond(this.reply(plainStudy));
      }

      this.stateInit = (plainStudy, participantUniqueId) => {
        this.expectStudy(plainStudy);
        this.gotoUrl(`/collection/${plainStudy.slug}/participants/add/${participantUniqueId}`);
        this.$httpBackend.flush();
        expect(this.$state.current.name).toBe('home.collection.study.participantAdd');
      };

      this.createStudies = (numStudies) =>
        _.range(numStudies).map(() => this.Study.create(self.Factory.study()));

      this.createGetStudiesFn = (studies) =>
        (pagerOptions) => this.$q.when(Object.assign(this.Factory.pagedResult(studies, pagerOptions),
                                               { items: studies.slice(0, pagerOptions.limit) }));
    });
  });

  afterEach(function() {
    this.$httpBackend.verifyNoOutstandingExpectation()
    this.$httpBackend.verifyNoOutstandingRequest()
  });

  it('has valid scope', function() {
    this.createController();

    expect(this.controller.study).toBe(this.study);
    expect(this.controller.uniqueId).toBe(this.uniqueId);

    expect(this.controller.participant).toEqual(jasmine.any(this.Participant));

    expect(this.controller.submit).toBeFunction();
    expect(this.controller.cancel).toBeFunction();
  });

  it('state configuration is valid', function() {
    this.stateInit(this.jsonStudy, this.Factory.stringNext());
  });

  describe('on submit', function() {

    beforeEach(function() {
      this.participant = new this.Participant(this.jsonParticipant);
      this.stateInit(this.jsonStudy, this.participant.uniqueId);
      this.createController();

    });

    it('changes to correct state for a valid new participant', function() {
      this.$httpBackend
        .expectPOST(this.url('participants', this.participant.studyId))
        .respond(this.jsonParticipant);

      this.$httpBackend
        .expectGET(this.url('participants', this.participant.slug))
        .respond(this.jsonParticipant);

      this.controller.submit(this.participant);
      this.$httpBackend.flush();

      expect(this.$state.current.name).toBe('home.collection.study.participant.summary');
    });

    describe('when submit fails', function() {

      it('displays an error modal', function() {
        this.$httpBackend
          .expectPOST(this.url('participants', this.participant.studyId))
          .respond(400, this.errorReply('simulated error'));
        spyOn(this.domainNotificationService, 'updateErrorModal').and.returnValue(this.$q.when('OK'));

        this.controller.submit(this.participant);
        this.$httpBackend.flush();

        expect(this.domainNotificationService.updateErrorModal).toHaveBeenCalled();
        expect(this.$state.current.name).toBe('home.collection.study.participantAdd');
      });

      it('changes to correct state when user presses the Cancel button on the error modal', function() {
        this.$httpBackend
          .expectPOST(this.url('participants', this.participant.studyId))
          .respond(400, this.errorReply('simulated error'));

        spyOn(this.domainNotificationService, 'updateErrorModal')
          .and.returnValue(this.$q.reject('cancel'));

        this.controller.submit(this.participant);
        this.$httpBackend.flush();
        expect(this.$state.current.name).toBe('home.collection.study');
      });

    });

  });

  it('changes to correct state on cancel', function() {
    this.stateInit(this.jsonStudy, this.jsonParticipant.uniqueId);
    this.createController();
    this.controller.cancel();
    this.scope.$digest();
    expect(this.$state.current.name).toBe('home.collection.study');

  });

});
