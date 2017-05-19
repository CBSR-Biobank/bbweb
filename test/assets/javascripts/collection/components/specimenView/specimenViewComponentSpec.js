/**
 * Jasmine test suite
 *
 */
define(function (require) {
  'use strict';

  var mocks = require('angularMocks'),
      _     = require('lodash');

  describe('specimenViewComponent', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(ComponentTestSuiteMixin) {
      var self = this;

      _.extend(this, ComponentTestSuiteMixin.prototype);
      self.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              'Study',
                              'Participant',
                              'CollectionEventType',
                              'CollectionEvent',
                              'Specimen',
                              'specimenAddModal',
                              'domainNotificationService',
                              'factory');
      this.putHtmlTemplates(
        '/assets/javascripts/collection/components/specimenView/specimenView.html',
        '/assets/javascripts/common/directives/statusLine/statusLine.html');

      this.createController = function (study,
                                        participant,
                                        collectionEventType,
                                        collectionEvent,
                                        specimen) {
        ComponentTestSuiteMixin.prototype.createScope.call(
          self,
          '<specimen-view study="vm.study"' +
            '             participant="vm.participant"' +
            '             collection-event-type="vm.collectionEventType"' +
            '             collection-event="vm.collectionEvent"' +
            '             specimen="vm.specimen">' +
            '<specimen-view>',
          {
            study:               study,
            participant:         participant,
            collectionEventType: collectionEventType,
            collectionEvent:     collectionEvent,
            specimen:            specimen
          },
          'specimenView');
      };

      // the object must have keys in same order as the parameters for createController()
      this.createEntities = function () {
        var rawSpecimenDescription = self.factory.collectionSpecimenDescription(),
            rawCollectionEventType = self.factory.collectionEventType(
              { specimenDescriptions: [ rawSpecimenDescription ]}),
            collectionEventType = self.CollectionEventType.create(rawCollectionEventType),
            collectionEvent = self.CollectionEvent(self.factory.collectionEvent(),
                                                       self.collectionEventType),
            specimen = new self.Specimen(self.factory.specimen(),
                                         collectionEventType.specimenDescriptions[0]),
            participant = new self.Participant(self.factory.defaultParticipant()),
            study = new self.Study(this.factory.defaultStudy());

        return {
            study:               study,
            participant:         participant,
            collectionEventType: collectionEventType,
            collectionEvent:     collectionEvent,
            specimen:            specimen
        };
      };
    }));

    it('has valid scope', function() {
      var entities = this.createEntities();
      this.createController.apply(this, _.values(entities));
      expect(this.controller.specimenDescription).toBeDefined();
    });

    it('user can return to previous page', function() {
      var entities = this.createEntities();

      this.injectDependencies('$state');
      spyOn(this.$state, 'go').and.returnValue(null);

      this.createController.apply(this, _.values(entities));
      this.controller.back();

      expect(this.$state.go).toHaveBeenCalledWith('^');
    });


  });

});
