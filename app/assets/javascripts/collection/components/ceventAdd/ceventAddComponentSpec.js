/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';

describe('Component: ceventAdd', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(ComponentTestSuiteMixin) {
      _.extend(this, ComponentTestSuiteMixin);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              '$state',
                              'domainNotificationService',
                              'Study',
                              'Participant',
                              'CollectionEvent',
                              'CollectionEventType',
                              'factory');

      this.jsonCevent      = this.factory.collectionEvent();
      this.jsonCeventType  = this.factory.defaultCollectionEventType();
      this.jsonParticipant = this.factory.defaultParticipant();
      this.jsonStudy       = this.factory.defaultStudy();

      this.study               = new this.Study(this.jsonStudy);
      this.participant         = new this.Participant(this.jsonParticipant);
      this.collectionEventType = new this.CollectionEventType(this.jsonCeventType);
      this.collectionEvent     = new this.CollectionEvent(this.jsonCevent);

      this.createController = () =>
        ComponentTestSuiteMixin.createController.call(
          this,
          `<cevent-add
            study="vm.study"
            participant="vm.participant"
            collection-event-type="vm.collectionEventType">
          </cevent-add>`,
          {
            study:               this.study,
            participant:         this.participant,
            collectionEventType: this.collectionEventType
          },
          'ceventAdd');
    });
  });

  it('has valid scope', function() {
    this.createController();

    expect(this.controller.study).toBe(this.study);
    expect(this.controller.participant).toBe(this.participant);
    expect(this.controller.collectionEventType).toBe(this.collectionEventType);

    expect(this.controller.collectionEvent).toBeDefined();
    expect(this.controller.title).toBeDefined();
    expect(this.controller.timeCompleted).toBeDate();

    expect(this.controller.submit).toBeFunction();
    expect(this.controller.cancel).toBeFunction();
  });

  it('on submit success changes state', function() {
    spyOn(this.CollectionEvent.prototype, 'add').and.returnValue(this.$q.when(this.collectionEvent));
    spyOn(this.$state, 'go').and.returnValue('ok');

    this.createController();
    this.controller.submit();
    this.scope.$digest();

    expect(this.$state.go).toHaveBeenCalledWith(
      'home.collection.study.participant.cevents.details',
      { collectionEventId: this.collectionEvent.id },
      { reload: true });
  });

  describe('on submit failure', function() {

    it('displays an error modal', function() {
      this.createController();
      spyOn(this.CollectionEvent.prototype, 'add')
        .and.returnValue(this.$q.reject('simulated update failure'));
      spyOn(this.domainNotificationService, 'updateErrorModal').and.returnValue(this.$q.when('ok'));

      this.controller.submit();
      this.scope.$digest();

      expect(this.domainNotificationService.updateErrorModal).toHaveBeenCalled();
    });

    it('changes state when Cancel button pressed on error modal', function() {
      this.createController();
      spyOn(this.CollectionEvent.prototype, 'add')
        .and.returnValue(this.$q.reject('simulated update failure'));
      spyOn(this.domainNotificationService, 'updateErrorModal')
        .and.returnValue(this.$q.reject('cancel button pressed'));
      spyOn(this.$state, 'go').and.returnValue('ok');

      this.controller.submit();
      this.scope.$digest();

      expect(this.$state.go).toHaveBeenCalledWith(
        'home.collection.study.participant', { participantId: this.participant.id });
    });

  });

  it('changes state when forms Cancel button is pressed', function() {
    spyOn(this.$state, 'go').and.returnValue('ok');

    this.createController();
    this.controller.cancel();
    this.scope.$digest();

    expect(this.$state.go).toHaveBeenCalledWith('home.collection.study.participant.cevents');
  });

  it('time completed is updated when edited', function() {
    var timeNow = new Date();

    this.createController();
    this.controller.dateTimeOnEdit(timeNow);
    this.scope.$digest();

    expect(this.controller.timeCompleted).toEqual(timeNow);
  });

});
