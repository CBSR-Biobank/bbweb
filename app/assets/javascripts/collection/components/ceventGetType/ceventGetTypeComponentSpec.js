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
                              'CollectionEventTypeName',
                              'Factory');

      this.jsonCeventTypes = _.range(2).map(() => this.Factory.collectionEventType());
      this.jsonParticipant = this.Factory.participant();
      this.jsonStudy       = this.Factory.defaultStudy();

      this.collectionEventTypes =
        this.jsonCeventTypes.map((jsonCeventType) => this.CollectionEventType.create(jsonCeventType));

      this.eventTypeNames = this.jsonCeventTypes.map((eventType) => ({
        id:   eventType.id,
        name: eventType.name
      }));

      this.CollectionEventTypeName.list =
        jasmine.createSpy().and.returnValue(this.$q.when(this.eventTypeNames));

      this.participant = new this.Participant(this.jsonParticipant);
      this.study       = new this.Study(this.jsonStudy);

      TestUtils.addCustomMatchers();

      spyOn(this.$state, 'go').and.returnValue(null);

      this.createController = () =>
        ComponentTestSuiteMixin.createController.call(
          this,
          `<cevent-get-type study="vm.study"
                            participant="vm.participant">
           </cevent-get-type>`,
          {
            study:       this.study,
            participant: this.participant
          },
          'ceventGetType');
    });
  });

  it('has valid scope', function() {
    this.createController();

    expect(this.controller.study).toBe(this.study);
    expect(this.controller.participant).toBe(this.participant);

    expect(this.controller.collectionEventTypeNames).toBeArrayOfSize(this.collectionEventTypes.length);
    [ 'id', 'name' ].forEach(attr => {
      expect(_.map(this.controller.collectionEventTypeNames, attr))
        .toContainAll(_.map(this.eventTypeNames, attr));
    })

    expect(this.controller.title).toBeDefined();
    expect(this.controller.updateCollectionEventType).toBeFunction();
  });

  describe('when collection event type is updated', function() {

    it('changes to correct state selection is valid', function() {
      const ceventTypeSlug = this.collectionEventTypes[0].slug;

      this.createController();
      this.controller.eventTypeSlug = ceventTypeSlug;
      this.controller.updateCollectionEventType();
      this.scope.$digest();

      expect(this.$state.go).toHaveBeenCalledWith('home.collection.study.participant.cevents.add.details',
                                                  { eventTypeSlug: ceventTypeSlug });
    });

  });

});
