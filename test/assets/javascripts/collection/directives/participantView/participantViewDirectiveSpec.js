/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'lodash'
], function(angular, mocks, _) {
  'use strict';

  describe('participantViewDirective', function() {

    var createDirective = function () {
      this.element = angular.element([
        '<participant-view',
        ' study="vm.study"',
        ' participant="vm.participant">',
        '</participant-view>'
      ].join(''));

      this.scope = this.$rootScope.$new();
      this.scope.vm = {
        study:       this.study,
        participant: this.participant
      };
      this.$compile(this.element)(this.scope);
      this.scope.$digest();
      this.controller = this.element.controller('participantView');
    };

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(TestSuiteMixin) {
      var self = this;

      _.extend(self, TestSuiteMixin.prototype);

      self.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              '$state',
                              'domainNotificationService',
                              'modalService',
                              'Study',
                              'Participant',
                              'factory');

      self.putHtmlTemplates(
        '/assets/javascripts/collection/directives/participantView/participantView.html',
        '/assets/javascripts/common/components/breadcrumbs/breadcrumbs.html');

      self.jsonParticipant = self.factory.participant();
      self.jsonStudy       = self.factory.defaultStudy();
      self.participant     = new self.Participant(self.jsonParticipant);
      self.study           = new self.Study(self.jsonStudy);

      spyOn(this.$state, 'go').and.returnValue('ok');
    }));

    it('has valid scope', function() {
      createDirective.call(this);

      expect(this.controller.study).toBe(this.study);
      expect(this.controller.participant).toBe(this.participant);

      expect(this.controller.tabs).toBeDefined();
    });

    it('has valid tabs', function() {
      createDirective.call(this);

      expect(this.controller.tabs).toBeArrayOfSize(2);
    });

    it('should initialize the tab of the current state', function() {
      var self = this,
          states,
          tab;

      createDirective.call(self);

      states = [
        'home.collection.study.participant.summary',
        'home.collection.study.participant.cevents'
      ];

      _.each(states, function (state) {
        self.$state.current.name = state;

        createDirective.call(self);
        tab = _.find(self.controller.tabs, { sref: state });
        expect(tab.active).toBe(true);
      });
    });

  });

});
