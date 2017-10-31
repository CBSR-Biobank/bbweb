/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';
import ngModule from '../../index'

describe('Component: ceventGetType', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function(ComponentTestSuiteMixin, TestUtils) {
      _.extend(this, ComponentTestSuiteMixin);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              '$state',
                              'Study',
                              'Participant',
                              'CollectionEventType',
                              'Factory');

      this.jsonCeventTypes = _.range(2).map(() => this.Factory.collectionEventType());
      this.jsonParticipant = this.Factory.participant();
      this.jsonStudy       = this.Factory.defaultStudy();

      this.collectionEventTypes =
        this.jsonCeventTypes.map((jsonCeventType) => this.CollectionEventType.create(jsonCeventType));

      this.participant = new this.Participant(this.jsonParticipant);
      this.study       = new this.Study(this.jsonStudy);

      TestUtils.addCustomMatchers();

      spyOn(this.$state, 'go').and.returnValue(null);

      this.createController = () =>
        ComponentTestSuiteMixin.createController.call(
          this,
          `<cevent-get-type
             study="vm.study"
             participant="vm.participant"
             collection-event-types="vm.collectionEventTypes">
           </cevent-get-type>`,
          {
            study:                this.study,
            participant:          this.participant,
            collectionEventTypes: this.collectionEventTypes
          },
          'ceventGetType');
    });
  });

  it('has valid scope', function() {
    this.createController();

    expect(this.controller.study).toBe(this.study);
    expect(this.controller.participant).toBe(this.participant);
    expect(this.controller.collectionEventTypes).toContainAll(this.collectionEventTypes);

    expect(this.controller.title).toBeDefined();
    expect(this.controller.collectionEvent).toBeDefined();

    expect(this.controller.updateCollectionEventType).toBeFunction();
  });

  describe('when collection event type is updated', function() {

    it('changes to correct state selection is valid', function() {
      var ceventTypeId = this.collectionEventTypes[0].id;

      this.createController();
      this.controller.collectionEvent.collectionEventTypeId = ceventTypeId;
      this.controller.updateCollectionEventType();
      this.scope.$digest();

      expect(this.$state.go).toHaveBeenCalledWith(
        'home.collection.study.participant.cevents.add.details',
        { collectionEventTypeId: ceventTypeId });
    });

    it('does nothing when selection is not valid', function() {
      this.createController();
      this.controller.collectionEvent.collectionEventTypeId = undefined;
      this.controller.updateCollectionEventType();
      this.scope.$digest();

      expect(this.$state.go).not.toHaveBeenCalled();
    });

  });

});
