/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import { TestSuiteMixin } from 'test/mixins/TestSuiteMixin';
import _ from 'lodash';
import ngModule from '../../index'

describe('studyCollectionComponent', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      var jsonStudy, jsonCet;

      Object.assign(this, TestSuiteMixin);
      this.injectDependencies('$rootScope',
                              '$compile',
                              '$q',
                              'Study',
                              'CollectionEventType',
                              'Factory');

      jsonStudy = this.Factory.study();
      jsonCet   = this.Factory.collectionEventType(jsonStudy);

      this.study = new this.Study(jsonStudy);
      this.collectionEventType = new this.CollectionEventType(jsonCet);

      spyOn(this.CollectionEventType, 'list').and.returnValue(this.$q.when([ this.collectionEventType ]));

      this.createController = (study, ceTypes) => {
        study = study || this.study;
        ceTypes = ceTypes || [];

        this.CollectionEventType.list = jasmine.createSpy().and
          .returnValue(this.$q.when(this.Factory.pagedResult(ceTypes)));
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
        basename = self.Factory.stringNext(),
        numCeTypes = 3,
        ceTypes = _.times(numCeTypes, function (index) {
          var ceType = self.CollectionEventType.create(self.Factory.collectionEventType());
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
        ceType = this.CollectionEventType.create(this.Factory.collectionEventType()),
        newName = this.Factory.stringNext();

    this.createController(this.study, [ ceType ]);
    childScope = this.element.isolateScope().$new();
    ceType.name = newName;
    childScope.$emit('collection-event-type-updated', ceType);
    this.scope.$digest();

    expect(this.controller.collectionEventTypes).toBeArrayOfSize(1);
    expect(this.controller.collectionEventTypes[0].name).toBe(newName);
  });

});
