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

describe('Component: participantView', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, ComponentTestSuiteMixin);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              '$state',
                              'domainNotificationService',
                              'modalService',
                              'Study',
                              'Participant',
                              'Factory');

      this.jsonParticipant = this.Factory.participant();
      this.jsonStudy       = this.Factory.defaultStudy();
      this.participant     = new this.Participant(this.jsonParticipant);
      this.study           = new this.Study(this.jsonStudy);

      spyOn(this.$state, 'go').and.returnValue('ok');

      this.createController = function (study, participant) {
        study = study || this.study;
        participant = participant || this.participant;

        this.createControllerInternal(
          `<participant-view
            study="vm.study"
            participant="vm.participant">
           </participant-view>`,
          {
            study: study,
            participant: participant
          },
          'participantView');
      };
    });
  });

  it('has valid scope', function() {
    this.createController();

    expect(this.controller.study).toBe(this.study);
    expect(this.controller.participant).toBe(this.participant);

    expect(this.controller.tabs).toBeDefined();
  });

  it('has valid tabs', function() {
    this.createController();

    expect(this.controller.tabs).toBeArrayOfSize(2);
  });

  it('should initialize the tab of the current state', function() {
    var self = this,
        states,
        tab;

    this.createController();

    states = [
      'home.collection.study.participant.summary',
      'home.collection.study.participant.cevents'
    ];

    states.forEach((state) => {
      self.$state.current.name = state;
      self.createController();
      tab = _.find(self.controller.tabs, { sref: state });
      expect(tab.active).toBe(true);
    });
  });

});
