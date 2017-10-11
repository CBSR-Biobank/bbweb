/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';

describe('studyCollectionComponent', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(TestSuiteMixin) {
      var jsonStudy, jsonCet;

      _.extend(this, TestSuiteMixin);
      this.injectDependencies('$rootScope',
                              '$compile',
                              '$q',
                              'Study',
                              'CollectionEventType',
                              'factory');

      jsonStudy = this.factory.study();
      jsonCet   = this.factory.collectionEventType(jsonStudy);

      this.study = new this.Study(jsonStudy);
      this.collectionEventType = new this.CollectionEventType(jsonCet);

      spyOn(this.CollectionEventType, 'list').and.returnValue(this.$q.when([ this.collectionEventType ]));

      this.createController = (study, ceTypes) => {
        study = study || this.study;
        ceTypes = ceTypes || [];

        this.CollectionEventType.list = jasmine.createSpy().and
          .returnValue(this.$q.when(this.factory.pagedResult(ceTypes)));
        this.element = angular.element('<study-collection study="study"></study-collection>');
        this.scope = this.$rootScope.$new();
        this.scope.study = study;

        this.eventRxFunc = jasmine.createSpy().and.returnValue(null);
        this.scope.$on('tabbed-page-update', this.eventRxFunc);

        this.$compile(this.element)(this.scope);
        this.scope.$digest();
        this.controller = this.element.controller('studyCollection');
      };
    });
  });

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
    childScope.$emit('collection-event-type-updated', ceType);
    this.scope.$digest();

    expect(this.controller.collectionEventTypes).toBeArrayOfSize(1);
    expect(this.controller.collectionEventTypes[0].name).toBe(newName);
  });

});
