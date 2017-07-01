/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var mocks = require('angularMocks'),
      _     = require('lodash');


  describe('Component: participantView', function() {

    function SuiteMixinFactory(ComponentTestSuiteMixin) {

      function SuiteMixin() {
      }

      SuiteMixin.prototype = Object.create(ComponentTestSuiteMixin.prototype);
      SuiteMixin.prototype.constructor = SuiteMixin;

      SuiteMixin.prototype.createController = function (study, participant) {
        study = study || this.study;
        participant = participant || this.participant;

        ComponentTestSuiteMixin.prototype.createController.call(
          this,
          [
            '<participant-view',
            ' study="vm.study"',
            ' participant="vm.participant">',
            '</participant-view>'
          ].join(''),
          {
            study: study,
            participant: participant
          },
          'participantView');
      };

      return SuiteMixin;
    }

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(ComponentTestSuiteMixin) {
      _.extend(this, new SuiteMixinFactory(ComponentTestSuiteMixin).prototype);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              '$state',
                              'domainNotificationService',
                              'modalService',
                              'Study',
                              'Participant',
                              'factory');

      this.putHtmlTemplates(
        '/assets/javascripts/collection/components/participantView/participantView.html',
        '/assets/javascripts/common/components/breadcrumbs/breadcrumbs.html');

      this.jsonParticipant = this.factory.participant();
      this.jsonStudy       = this.factory.defaultStudy();
      this.participant     = new this.Participant(this.jsonParticipant);
      this.study           = new this.Study(this.jsonStudy);

      spyOn(this.$state, 'go').and.returnValue('ok');
    }));

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

      _.each(states, function (state) {
        self.$state.current.name = state;
        self.createController();
        tab = _.find(self.controller.tabs, { sref: state });
        expect(tab.active).toBe(true);
      });
    });

  });

});
