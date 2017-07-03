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

  describe('studyCollectionComponent', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(TestSuiteMixin) {
      var self = this, jsonStudy, jsonCet;

      _.extend(self, TestSuiteMixin.prototype);

      self.injectDependencies('$rootScope',
                              '$compile',
                              '$q',
                              'Study',
                              'CollectionEventType',
                              'factory');

      jsonStudy = self.factory.study();
      jsonCet   = self.factory.collectionEventType(jsonStudy);

      self.study = new self.Study(jsonStudy);
      self.collectionEventType = new self.CollectionEventType(jsonCet);

      spyOn(self.CollectionEventType, 'list').and.returnValue(self.$q.when([ self.collectionEventType ]));

      self.putHtmlTemplates(
        '/assets/javascripts/admin/studies/components/studyCollection/studyCollection.html',
        '/assets/javascripts/admin/studies/components/studyNotDisabledWarning/studyNotDisabledWarning.html',
        '/assets/javascripts/admin/studies/components/ceventTypesAddAndSelect/ceventTypesAddAndSelect.html');

      this.createController = function (study, ceTypes) {
        study = study || this.study;
        ceTypes = ceTypes || [];

        self.CollectionEventType.list = jasmine.createSpy().and
          .returnValue(self.$q.when(self.factory.pagedResult(ceTypes)));
        self.element = angular.element('<study-collection study="study"></study-collection>');
        self.scope = self.$rootScope.$new();
        self.scope.study = study;

        self.eventRxFunc = jasmine.createSpy().and.returnValue(null);
        self.scope.$on('tabbed-page-update', self.eventRxFunc);

        self.$compile(self.element)(self.scope);
        self.scope.$digest();
        self.controller = self.element.controller('studyCollection');
      };
    }));

    it('initialization is valid', function() {
      var self = this,
          basename = self.factory.stringNext(),
          numCeTypes = 3,
          ceTypes = _.times(numCeTypes, function (index) {
            var ceType = self.CollectionEventType.create(self.factory.collectionEventType());
            ceType.name = basename + '_' + (numCeTypes - index);
            return ceType;
          });

      this.createController(this.study, ceTypes);
      expect(this.controller.study).toBe(this.study);
      expect(this.eventRxFunc).toHaveBeenCalled();
      expect(this.controller.collectionEventTypes).toBeArrayOfSize(numCeTypes);

      // ceTypes must be sorted by name
      expect(this.controller.collectionEventTypes[0].name).toBe(ceTypes[2].name);
      expect(this.controller.collectionEventTypes[1].name).toBe(ceTypes[1].name);
      expect(this.controller.collectionEventTypes[2].name).toBe(ceTypes[0].name);
    });

    it('should update the Collection Event Types when event is emitted', function() {
      var childScope,
          ceType = this.CollectionEventType.create(this.factory.collectionEventType()),
          newName = this.factory.stringNext();

      this.createController(this.study, [ ceType ]);
      childScope = this.element.isolateScope().$new();
      ceType.name = newName;
      childScope.$emit('collection-event-type-name-changed', ceType);
      this.scope.$digest();

      expect(this.controller.collectionEventTypes).toBeArrayOfSize(1);
      expect(this.controller.collectionEventTypes[0].name).toBe(newName);
    });

  });

});
