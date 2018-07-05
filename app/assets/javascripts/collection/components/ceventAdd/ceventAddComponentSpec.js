/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import { ComponentTestSuiteMixin } from 'test/mixins/ComponentTestSuiteMixin';
import ngModule from '../../index'

describe('Component: ceventAdd', function() {

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
                              'CollectionEvent',
                              'CollectionEventType',
                              'Factory');

      this.jsonCevent      = this.Factory.collectionEvent();
      this.jsonCeventType  = this.Factory.defaultCollectionEventType();
      this.jsonParticipant = this.Factory.defaultParticipant();
      this.jsonStudy       = this.Factory.defaultStudy();

      this.study               = new this.Study(this.jsonStudy);
      this.participant         = new this.Participant(this.jsonParticipant);
      this.collectionEventType = new this.CollectionEventType(this.jsonCeventType);
      this.collectionEvent     = new this.CollectionEvent(this.jsonCevent);

      this.createController = (collectionEventType = this.collectionEventType) => {
        this.CollectionEventType.get =
          jasmine.createSpy().and.returnValue(this.$q.when(this.$q.when(collectionEventType)))
        this.createControllerInternal(
          `<cevent-add study="vm.study"
                       participant="vm.participant"
                       collection-event-type="vm.collectionEventType">
           </cevent-add>`,
          {
            study:               this.study,
            participant:         this.participant,
            collectionEventType: collectionEventType
          },
          'ceventAdd');
      }
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
      { visitNumber: this.collectionEvent.visitNumber },
      { reload: true });
  });

  describe('on submit failure', function() {

    it('displays an error modal for invalid visit number', function() {
      this.createController();
      this.CollectionEvent.prototype.add = jasmine.createSpy()
        .and.returnValue(this.$q.reject({
          message: 'EntityCriteriaError: a collection event with this visit number already exists'
        }));
      this.modalService.modalOk = jasmine.createSpy().and.returnValue(this.$q.when('ok'));

      this.controller.submit();
      this.scope.$digest();

      expect(this.modalService.modalOk).toHaveBeenCalled();
    });

    it('when error is not an invalid visit #, changes state when Cancel button pressed on error modal',
        function() {
          this.createController();

          this.CollectionEvent.prototype.add = jasmine.createSpy()
            .and.returnValue(this.$q.reject({ message: 'simulated update failure' }));
          this.domainNotificationService.updateErrorModal = jasmine.createSpy()
            .and.returnValue(this.$q.reject('cancel button pressed'));
          this.$state.go = jasmine.createSpy().and.returnValue(null);

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
