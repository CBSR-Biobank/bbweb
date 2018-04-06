/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import { EntityTestSuiteMixin } from 'test/mixins/EntityTestSuiteMixin';
import { ServerReplyMixin } from 'test/mixins/ServerReplyMixin';
import faker from 'faker';
import ngModule from '../../index'

describe('StudyCounts', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, EntityTestSuiteMixin, ServerReplyMixin);
      this.injectDependencies('StudyCounts', '$httpBackend');
    });
  });

  afterEach(function() {
    this.$httpBackend.verifyNoOutstandingExpectation();
    this.$httpBackend.verifyNoOutstandingRequest();
  });

  function fakeStudyCounts() {
    var disabled = faker.random.number(),
        enabled = faker.random.number(),
        retired = faker.random.number();
    return {
      total:    disabled + enabled + retired,
      disabledCount: disabled,
      enabledCount:  enabled,
      retiredCount:  retired
    };
  }

  it('can get created from empty object', function() {
    var counts = new this.StudyCounts();
    expect(counts.total).toEqual(0);
    expect(counts.disabled).toEqual(0);
    expect(counts.enabled).toEqual(0);
    expect(counts.retired).toEqual(0);
  });

  it('can get study counts from server', function() {
    var counts = fakeStudyCounts();
    this.$httpBackend.whenGET('/api/studies/counts').respond(this.reply(counts));
    this.StudyCounts.get().then(expectCounts).catch(failTest);
    this.$httpBackend.flush();

    function expectCounts(replyCounts) {
      expect(replyCounts.total).toEqual(counts.total);
      expect(replyCounts.disabled).toEqual(counts.disabledCount);
      expect(replyCounts.enabled).toEqual(counts.enabledCount);
      expect(replyCounts.retired).toEqual(counts.retiredCount);
    }

    function failTest(error) {
      expect(error).toBeUndefined();
    }
  });

});
