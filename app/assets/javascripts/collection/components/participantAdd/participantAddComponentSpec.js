/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';
import ngModule from '../../index'

describe('Component: participantAdd', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function(ComponentTestSuiteMixin) {
      _.extend(this, ComponentTestSuiteMixin);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
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

      spyOn(this.$state, 'go').and.returnValue('ok');

      this.createController = (study, uniqueId) => {
        study = study || this.study;
        uniqueId = uniqueId || this.uniqueId;

        ComponentTestSuiteMixin.createController.call(
          this,
          `<participant-add
             study="vm.study"
             unique-id="${uniqueId}">
           </participant-add>`,
          { study: study },
          'participantAdd');
      };

      this.createStudies = (numStudies) =>
        _.range(numStudies).map(() => this.Study.create(self.Factory.study()));

      this.createGetStudiesFn = (studies) =>
        (pagerOptions) => this.$q.when(_.extend(this.Factory.pagedResult(studies, pagerOptions),
                                               { items: studies.slice(0, pagerOptions.limit) }));
    });
  });

  it('has valid scope', function() {
    this.createController();

    expect(this.controller.study).toBe(this.study);
    expect(this.controller.uniqueId).toBe(this.uniqueId);

    expect(this.controller.participant).toEqual(jasmine.any(this.Participant));

    expect(this.controller.submit).toBeFunction();
    expect(this.controller.cancel).toBeFunction();
  });

  describe('on submit', function() {

    beforeEach(function() {
      this.participant = new this.Participant(this.jsonParticipant);
    });

    it('changes to correct state on valid submit', function() {
      spyOn(this.Participant.prototype, 'add').and.returnValue(this.$q.when(this.participant));

      this.createController();
      this.controller.submit(this.participant);
      this.scope.$digest();

      expect(this.$state.go).toHaveBeenCalledWith(
        'home.collection.study.participant.summary',
        { studyId: this.study.id, participantId: this.participant.id },
        { reload: true });
    });

    describe('when submit fails', function() {

      it('displays an error modal', function() {
        this.createController();

        spyOn(this.Participant.prototype, 'add').and.returnValue(this.$q.reject('submit failure'));
        spyOn(this.domainNotificationService, 'updateErrorModal').and.returnValue(this.$q.when('OK'));

        this.controller.submit(this.participant);
        this.scope.$digest();

        expect(this.domainNotificationService.updateErrorModal).toHaveBeenCalled();
      });

      it('user presses Cancel on error modal', function() {
        this.createController();

        spyOn(this.Participant.prototype, 'add').and.returnValue(this.$q.reject('submit failure'));
        spyOn(this.domainNotificationService, 'updateErrorModal')
          .and.returnValue(this.$q.reject('Cancel'));

        this.controller.submit(this.participant);
        this.scope.$digest();

        expect(this.domainNotificationService.updateErrorModal).toHaveBeenCalled();
        expect(this.$state.go).toHaveBeenCalledWith(
          'home.collection.study',
          { studyId: this.study.id });
      });

    });

  });

  it('changes to correct state on cancel', function() {
    this.createController();
    this.controller.cancel();
    this.scope.$digest();

    expect(this.$state.go).toHaveBeenCalledWith('home.collection.study',
                                                { studyId: this.study.id });

  });

});
