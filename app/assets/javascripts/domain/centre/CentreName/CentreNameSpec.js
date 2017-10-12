/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';
import sharedBehaviour from '../../../test/entityNameSharedBehaviour';

describe('CentreName', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(EntityTestSuite,
                                 ServerReplyMixin) {
      _.extend(this, EntityTestSuite.prototype, ServerReplyMixin.prototype);

      this.injectDependencies('$httpBackend',
                              '$httpParamSerializer',
                              'EntityName',
                              'CentreName',
                              'CentreState',
                              'factory');
      // used by promise tests
      this.expectCentre = (entity) => {
        expect(entity).toEqual(jasmine.any(this.CentreName));
      };

      this.url = url;

      //---

      function url() {
        const args = [ 'centres/names' ].concat(_.toArray(arguments));
        return EntityTestSuite.prototype.url.apply(null, args);
      }
    });
  });

  afterEach(function() {
    this.$httpBackend.verifyNoOutstandingExpectation();
    this.$httpBackend.verifyNoOutstandingRequest();
  });

  describe('common behaviour', function() {

    var context = {};

    beforeEach(function() {
      context.constructor = this.CentreName;
      context.createFunc  = this.CentreName.create;
      context.restApiUrl  = this.url();
      context.factoryFunc = this.factory.centreNameDto;
      context.listFunc    = this.CentreName.list;
    });

    sharedBehaviour(context);

  });

  it('state predicates return valid results', function() {
    _.values(this.CentreState).forEach((state) => {
      var entityName = this.CentreName.create(this.factory.centreNameDto({ state: state }));
      expect(entityName.isDisabled()).toBe(state === this.CentreState.DISABLED);
      expect(entityName.isEnabled()).toBe(state === this.CentreState.ENABLED);
    });
  });

});
