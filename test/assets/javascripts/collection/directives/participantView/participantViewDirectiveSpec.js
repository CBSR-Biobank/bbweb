/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'underscore'
], function(angular, mocks, _) {
  'use strict';

  describe('participantViewDirective', function() {

    function createDirective(test) {
      var element,
          scope = test.$rootScope.$new();

      element = angular.element([
        '<participant-view',
        ' study="vm.study"',
        ' participant="vm.participant">',
        '</participant-view>'
      ].join(''));

      scope.vm = {
        study:       test.study,
        participant: test.participant
      };
      test.$compile(element)(scope);
      scope.$digest();

      return {
        element:    element,
        scope:      scope,
        controller: element.controller('participantView')
      };
    }

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(templateMixin) {
      var self = this;

      _.extend(self, templateMixin);

      self.$q                  = self.$injector.get('$q');
      self.$rootScope          = self.$injector.get('$rootScope');
      self.$compile            = self.$injector.get('$compile');
      self.$state              = self.$injector.get('$state');
      self.domainEntityService = self.$injector.get('domainEntityService');
      self.modalService        = self.$injector.get('modalService');
      self.Study               = self.$injector.get('Study');
      self.Participant         = self.$injector.get('Participant');
      self.factory        = self.$injector.get('factory');

      self.putHtmlTemplates(
        '/assets/javascripts/collection/directives/participantView/participantView.html');

      self.jsonParticipant = self.factory.participant();
      self.jsonStudy       = self.factory.defaultStudy();
      self.participant     = new self.Participant(self.jsonParticipant);
      self.study           = new self.Study(self.jsonStudy);

      spyOn(this.$state, 'go').and.returnValue('ok');
    }));

    it('has valid scope', function() {
      var directive = createDirective(this);

      expect(directive.controller.study).toBe(this.study);
      expect(directive.controller.participant).toBe(this.participant);

      expect(directive.controller.tabs).toBeDefined();
    });

    it('has valid tabs', function() {
      var directive = createDirective(this);

      expect(directive.controller.tabs).toBeArrayOfSize(2);
    });

    it('should initialize the tab of the current state', function() {
      var self = this,
          directive = createDirective(this),
          states,
          tab;

      states = [
        'home.collection.study.participant.summary',
        'home.collection.study.participant.cevents'
      ];

      _.each(states, function (state) {
        self.$state.current.name = state;

        directive = createDirective(self);
        tab = _.findWhere(directive.controller.tabs, { sref: state });
        expect(tab.active).toBe(true);
      });
    });

  });

});
