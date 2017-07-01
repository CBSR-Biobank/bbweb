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

  describe('Component: ceventGetType', function() {

    function SuiteMixinFactory(ComponentTestSuiteMixin) {

      function SuiteMixin() {
      }

      SuiteMixin.prototype = Object.create(ComponentTestSuiteMixin.prototype);
      SuiteMixin.prototype.constructor = SuiteMixin;

      SuiteMixin.prototype.createController = function () {
        ComponentTestSuiteMixin.prototype.createScope.call(
          this,
          [
            '<cevent-get-type',
            '  study="vm.study"',
            '  participant="vm.participant"',
            '  collection-event-types="vm.collectionEventTypes">',
            '</cevent-get-type>'
          ].join(''),
          {
            study:                this.study,
            participant:          this.participant,
            collectionEventTypes: this.collectionEventTypes
          },
          'ceventGetType');
      };

      return SuiteMixin;
    }

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(ComponentTestSuiteMixin, testUtils) {
      var self = this;

      _.extend(this, new SuiteMixinFactory(ComponentTestSuiteMixin).prototype);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              '$state',
                              'Study',
                              'Participant',
                              'CollectionEventType',
                              'factory');

      this.putHtmlTemplates(
        '/assets/javascripts/collection/components/ceventGetType/ceventGetType.html');

      this.jsonCeventTypes = _.map(_.range(2), function () {
        return self.factory.collectionEventType();
      });
      this.jsonParticipant = this.factory.participant();
      this.jsonStudy       = this.factory.defaultStudy();

      this.collectionEventTypes = _.map(this.jsonCeventTypes, function (jsonCeventType){
        return new self.CollectionEventType(jsonCeventType);
      });
      this.participant = new this.Participant(this.jsonParticipant);
      this.study       = new this.Study(this.jsonStudy);

      testUtils.addCustomMatchers();

      spyOn(this.$state, 'go').and.returnValue(null);
    }));

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

});
