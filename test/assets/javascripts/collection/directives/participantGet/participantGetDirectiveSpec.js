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

    var createDirective = function () {
      this.element = angular.element('<participant-get study="vm.study"></participant-get>');
      this.scope = this.$rootScope.$new();
      this.scope.vm = { study: this.study };
      this.$compile(this.element)(this.scope);
      this.scope.$digest();
      this.controller = this.element.controller('participantGet');
    };

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(testSuiteMixin) {
      var self = this;

      _.extend(self, testSuiteMixin);

      self.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              '$state',
                              'stateHelper',
                              'modalService',
                              'Study',
                              'Participant',
                              'factory');
      self.putHtmlTemplates(
        '/assets/javascripts/collection/directives/participantGet/participantGet.html');

      this.jsonParticipant = this.factory.participant();
      this.jsonStudy       = this.factory.defaultStudy();
      this.participant     = new this.Participant(this.jsonParticipant);
      this.study           = new this.Study(this.jsonStudy);
    }));

    it('has valid scope', function() {
      createDirective.call(this);

      expect(this.controller).toBeDefined();
      expect(this.controller.study).toBe(this.study);
      expect(this.controller.uniqueIdChanged).toBeFunction();
    });

    describe('when invoking uniqueIdChanged', function() {

      it('does nothing with an empty participant ID', function() {
        spyOn(this.Participant, 'getByUniqueId').and.returnValue(this.$q.when(this.participant));

        createDirective.call(this);
        this.controller.uniqueId = '';
        this.controller.uniqueIdChanged();
        this.scope.$digest();

        expect(this.Participant.getByUniqueId).not.toHaveBeenCalled();
      });

      it('with a valid participant ID changes state', function() {
        spyOn(this.Participant, 'getByUniqueId').and.returnValue(this.$q.when(this.participant));
        spyOn(this.$state, 'go').and.returnValue('ok');

        createDirective.call(this);
        this.controller.uniqueId = this.factory.stringNext();
        this.controller.uniqueIdChanged();
        this.scope.$digest();

        expect(this.$state.go).toHaveBeenCalledWith(
          'home.collection.study.participant.summary',
          { participantId: this.participant.id });
      });

      it('with an invalid participant ID opens a modal', function() {
        var uniqueId = this.factory.stringNext(),
            deferred = this.$q.defer();

        deferred.reject({ status: 404 });
        spyOn(this.Participant, 'getByUniqueId').and.returnValue(deferred.promise);
        spyOn(this.modalService, 'showModal').and.returnValue(this.$q.when('ok'));
        spyOn(this.$state, 'go').and.returnValue('ok');

        createDirective.call(this);
        this.controller.uniqueId = uniqueId;
        this.controller.uniqueIdChanged();
        this.scope.$digest();

        expect(this.modalService.showModal).toHaveBeenCalled();
        expect(this.$state.go).toHaveBeenCalledWith(
          'home.collection.study.participantAdd',
          { uniqueId: uniqueId });
      });

      it('with an invalid participant ID opens a modal and cancel is pressed', function() {
        var uniqueId = this.factory.stringNext(),
            participantDeferred = this.$q.defer(),
            modalDeferred = this.$q.defer();

        participantDeferred.reject({ status: 404 });
        modalDeferred.reject('Cancel');
        spyOn(this.Participant, 'getByUniqueId').and.returnValue(participantDeferred.promise);
        spyOn(this.modalService, 'showModal').and.returnValue(modalDeferred.promise);
        spyOn(this.stateHelper, 'reloadAndReinit').and.returnValue('ok');

        createDirective.call(this);
        this.controller.uniqueId = uniqueId;
        this.controller.uniqueIdChanged();
        this.scope.$digest();

        expect(this.modalService.showModal).toHaveBeenCalled();
        expect(this.stateHelper.reloadAndReinit).toHaveBeenCalled();
      });

      it('promise is rejected on a non 404 response', function() {
        var participantDeferred = this.$q.defer();

        participantDeferred.reject({ status: 400 });
        spyOn(this.Participant, 'getByUniqueId').and.returnValue(participantDeferred.promise);
        spyOn(console, 'error').and.callThrough();

        createDirective.call(this);
        this.controller.uniqueId = this.factory.stringNext();
        this.controller.uniqueIdChanged();
        this.scope.$digest();

        expect(console.error).toHaveBeenCalled();
      });

    });

  });

});
