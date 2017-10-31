/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';
import ngModule from '../../index'
import sharedBehaviour from '../../../test/behaviours/entityNameSharedBehaviour';

describe('CentreName', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function(EntityTestSuiteMixin,
                                 ServerReplyMixin) {
      _.extend(this, EntityTestSuiteMixin, ServerReplyMixin);

      this.injectDependencies('$httpBackend',
                              '$httpParamSerializer',
                              'EntityName',
                              'CentreName',
                              'CentreState',
                              'Factory');
      // used by promise tests
      this.expectCentre = (entity) => {
        expect(entity).toEqual(jasmine.any(this.CentreName));
      };

      this.url = url;

      //---

      function url() {
        const args = [ 'centres/names' ].concat(_.toArray(arguments));
        return EntityTestSuiteMixin.url.apply(null, args);
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
      context.factoryFunc = this.Factory.userNameDto.bind(this.Factory);
      context.listFunc    = this.CentreName.list;
    });

    sharedBehaviour(context);

  });

  it('state predicates return valid results', function() {
    _.values(this.CentreState).forEach((state) => {
      var entityName = this.CentreName.create(this.Factory.centreNameDto({ state: state }));
      expect(entityName.isDisabled()).toBe(state === this.CentreState.DISABLED);
      expect(entityName.isEnabled()).toBe(state === this.CentreState.ENABLED);
    });
  });

});
