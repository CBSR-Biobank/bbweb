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

  describe('participantGetDirective', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(directiveTestSuite) {
      var self = this;

      _.extend(self, directiveTestSuite);

      self.$q           = self.$injector.get('$q');
      self.$rootScope   = self.$injector.get('$rootScope');
      self.$compile     = self.$injector.get('$compile');
      self.$state       = this.$injector.get('$state');
      self.stateHelper  = this.$injector.get('stateHelper');
      self.modalService = self.$injector.get('modalService');
      self.Study        = self.$injector.get('Study');
      self.Participant  = self.$injector.get('Participant');
      self.jsonEntities = self.$injector.get('jsonEntities');

      self.putHtmlTemplates(
        '/assets/javascripts/collection/directives/participantGet/participantGet.html');

      this.jsonParticipant = this.jsonEntities.participant();
      this.jsonStudy       = this.jsonEntities.defaultStudy();
      this.participant     = new this.Participant(this.jsonParticipant);
      this.study           = new this.Study(this.jsonStudy);

      //--

    }));

    function createDirective(test) {
      var element = angular.element('<participant-get study="vm.study"></participant-get>'),
          scope = test.$rootScope.$new();

      scope.vm = { study: test.study };
      test.$compile(element)(scope);
      scope.$digest();

      return {
        element:    element,
        scope:      scope,
        controller: element.controller('participantGet')
      };
    }

    it('has valid scope', function() {
      var directive = createDirective(this);

      expect(directive.controller).toBeDefined();
      expect(directive.controller.study).toBe(this.study);
      expect(directive.controller.uniqueIdChanged).toBeFunction();
    });

    describe('when invoking uniqueIdChanged', function() {

      it('does nothing with an empty participant ID', function() {
        var directive;

        spyOn(this.Participant, 'getByUniqueId').and.returnValue(this.$q.when(this.participant));

        directive = createDirective(this);
        directive.controller.uniqueId = '';
        directive.controller.uniqueIdChanged();
        directive.scope.$digest();

        expect(this.Participant.getByUniqueId).not.toHaveBeenCalled();
      });

      it('with a valid participant ID changes state', function() {
        var directive;

        spyOn(this.Participant, 'getByUniqueId').and.returnValue(this.$q.when(this.participant));
        spyOn(this.$state, 'go').and.returnValue('ok');

        directive = createDirective(this);
        directive.controller.uniqueId = this.jsonEntities.stringNext();
        directive.controller.uniqueIdChanged();
        directive.scope.$digest();

        expect(this.$state.go).toHaveBeenCalledWith(
          'home.collection.study.participant.summary',
          { participantId: this.participant.id });
      });

      it('with an invalid participant ID opens a modal', function() {
        var directive,
            uniqueId = this.jsonEntities.stringNext(),
            deferred = this.$q.defer();

        deferred.reject({ status: 404 });
        spyOn(this.Participant, 'getByUniqueId').and.returnValue(deferred.promise);
        spyOn(this.modalService, 'showModal').and.returnValue(this.$q.when('ok'));
        spyOn(this.$state, 'go').and.returnValue('ok');

        directive = createDirective(this);
        directive.controller.uniqueId = uniqueId;
        directive.controller.uniqueIdChanged();
        directive.scope.$digest();

        expect(this.modalService.showModal).toHaveBeenCalled();
        expect(this.$state.go).toHaveBeenCalledWith(
          'home.collection.study.participantAdd',
          { uniqueId: uniqueId });
      });

      it('with an invalid participant ID opens a modal and cancel is pressed', function() {
        var directive,
            uniqueId = this.jsonEntities.stringNext(),
            participantDeferred = this.$q.defer(),
            modalDeferred = this.$q.defer();

        participantDeferred.reject({ status: 404 });
        modalDeferred.reject('Cancel');
        spyOn(this.Participant, 'getByUniqueId').and.returnValue(participantDeferred.promise);
        spyOn(this.modalService, 'showModal').and.returnValue(modalDeferred.promise);
        spyOn(this.stateHelper, 'reloadAndReinit').and.returnValue('ok');

        directive = createDirective(this);
        directive.controller.uniqueId = uniqueId;
        directive.controller.uniqueIdChanged();
        directive.scope.$digest();

        expect(this.modalService.showModal).toHaveBeenCalled();
        expect(this.stateHelper.reloadAndReinit).toHaveBeenCalled();
      });

      it('promise is rejected on a non 404 response', function() {
        var directive,
            participantDeferred = this.$q.defer();

        participantDeferred.reject({ status: 400 });
        spyOn(this.Participant, 'getByUniqueId').and.returnValue(participantDeferred.promise);
        spyOn(console, 'error').and.callThrough();

        directive = createDirective(this);
        directive.controller.uniqueId = this.jsonEntities.stringNext();
        directive.controller.uniqueIdChanged();
        directive.scope.$digest();

        expect(console.error).toHaveBeenCalled();
      });

    });

  });

});
